/* Copyright 2006-2007, Queensland University of Technology
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy of 
 * the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 * 
 * Author: Shaun Mangelsdorf
 * Creation Date: Aug 16, 2007
 * 
 * Purpose: Replacement for the original MarshallerOutput - now has a reference counted pointer.
 */

#ifndef SAMLDOCUMENT_H_
#define SAMLDOCUMENT_H_

#include "saml2/SAML2Defs.h"

#include <memory>
#include <iostream>
#include <cstring>

namespace saml2
{
	/*
	 * This file is called SAMLDocument.h despite declaring a type called ManagedDocument,
	 * because at the bottom there is a:
	 * typedef ManagedDocument<SAMLByte,long> SAMLDocument;
	 * 
	 * That is the only type the SAML2 library will use, so naming this file as such seemed more fitting.
	 */
	
	
	/**
	 * @class ManagedDocument
	 * @brief Container class for document data.
	 * 
	 * The copy and assignment operators for this object are NOT thread safe.
	 * Care must be taken to ensure that multiple threads are not allowed to copy
	 * this object simultaneously.
	 * 
	 * If the object is to be used across multiple threads, the copy() method
	 * must be called to create another instance of the document. While the copy
	 * method is not guaranteed to be thread safe itself, the result will be a
	 * copy of the document which can be used in another thread without affecting
	 * the original object.
	 */
	template <typename CharT, typename SizeT, typename RefCountT = unsigned int>
	class ManagedDocument
	{
		
		private:
		/**
		 * @class ManagedDocumentInternal
		 * @brief Implementation of a reference counted pointer to a buffer.
		 * 
		 * The document contained here will be automatically deleted when the last reference
		 * to it is released. This class is declared private so that it can never be
		 * instantiated anywhere other than in ManagedDocument - using this improperly can lead
		 * to undefined behaviour.
		 * 
		 * This object should never be explicitly deleted. Instead, release() should be called.
		 * After calling release() no code may rely on the object being valid, as release() may
		 * cause the object to delete itself.
		 */
		class ManagedDocumentInternal
		{
			
			public:
			/**
			 * Instantiates the document container.
			 * The pointer given must have been allocated by operator new[]. If this is not
			 * the case, the destructor will have undefined behaviour.
			 */
			ManagedDocumentInternal( CharT* data, SizeT length )
			:
			_data(data),
			_length(length),
			_refs(1)
			{}
			
			/**
			 * Deletes the references to the document. This will only be called
			 * when the last reference to the document has been released.
			 */
			~ManagedDocumentInternal()
			{
				if( this->_data != NULL )
				{
					delete[] this->_data;
				}
			}
			
			CharT* _data;
			SizeT _length;
			
			inline void release()
			{
				this->_refs--;
				
				if( this->_refs == 0 )
				{
					// Yes, I'm aware this is a piece of code not to be messed with.
					// There are 2 reasons this is OK:
					// 1. This is a private data type, only EVER used within the ManagedDocument.
					//   As such, I know that it will only ever exist on the heap, and that
					//   it is never assumed to still exist after release() has been called.
					// 2. This is the last thing that will be called in the method, so
					//   we don't need to care about any local variables being accessed again.
					delete this;
				}
			}
			
			inline ManagedDocumentInternal* newReference()
			{
				this->_refs++;
				
				return this;
			}
			
			private:
			ManagedDocumentInternal( const ManagedDocumentInternal& other );
			ManagedDocumentInternal& operator=( const ManagedDocumentInternal& other );
			RefCountT _refs;
		} *_document;
		
		public:
		/**
		 * Initializes this to a NULL document.
		 */
		ManagedDocument()
		:
		_document( NULL )
		{
		}
		
		/**
		 * Initializes this to the document given. Ownership of the given pointer is taken
		 * and it will be deleted when no references remain. It is of the utmost importance
		 * that:
		 * 
		 * 1. The pointer was constructed with the C++ array allocation operator new[]; and
		 * 2. The pointer is NOT being managed by ANY other memory management, including a
		 *   different instance of ManagedDocument.
		 */
		ManagedDocument( CharT* data, SizeT length )
		:
		_document( new ManagedDocumentInternal( data, length ) )
		{
		}
		
		/**
		 * Takes a copy of the document in the given @c ManagedDocument object.
		 */
		ManagedDocument( const ManagedDocument<CharT,SizeT,RefCountT>& other )
		:
		_document( other._document->newReference() )
		{
		}
		
		/**
		 * Releases this reference to the document. If there are no remaining references 
		 * the document is deleted.
		 */
		~ManagedDocument()
		{
			if( this->_document != NULL )
			{
				// this->_document is not guaranteed to be a valid pointer after this call.
				this->_document->release();
			}
		}
		
		/**
		 * Replaces the document reference owned by this object with a reference to the 
		 * document provided.
		 * 
		 * This method releases any previously held reference to a document. If there 
		 * are no remaining references the previously held document is deleted.
		 */
		ManagedDocument<CharT,SizeT,RefCountT>& operator=( const ManagedDocument<CharT,SizeT,RefCountT>& other )
		{
			// Handle self-assignment correctly
			if( &other == this )
			{
				return *this;
			}
			
			// Free the document we own before we overwrite it.
			if( this->_document != NULL )
			{
				// this->_document is not guaranteed to be a valid pointer after this call.
				this->_document->release();
			}
			
			// Copy the document from the other ManagedDocument.
			this->_document = other._document->newReference();
			
			return *this;
		}
		
		/**
		 * Returns a pointer to the data contained. May be NULL if this object has
		 * not been initialized.
		 * 
		 * Note that accessing this pointer outside the lifetime of the object that returned
		 * it will cause undefined behaviour, as the document is no longer guaranteed to exist.
		 */
		const CharT* getData() const
		{
			if( this->_document != NULL )
			{
				return this->_document->_data;
			}
			
			return NULL;
		}
		
		/**
		 * Returns the length of the data contained. This value cannot be relied on if
		 * getData() returns NULL.
		 */
		SizeT getLength() const
		{
			if( this->_document != NULL )
			{
				return this->_document->_length;
			}
			
			return 0;
		}
		
		/**
		 * Returns a complete copy of this document. The returned value will be mutually
		 * exclusive from this one, and subject to a completely new reference count, etc.
		 */
		ManagedDocument<CharT,SizeT,RefCountT> copy() const
		{
			// If we don't have a document, just return another empty SAMLDocument
			if( this->_document == NULL || this->_document->_data == NULL || this->_document->_length == 0 )
			{
				return ManagedDocument<CharT,SizeT,RefCountT>();
			}
			
			long length = this->_document->_length;
			SAMLByte *data = new SAMLByte[ length ];
			
			std::memcpy( data, this->_document->_data, length );
			return ManagedDocument<CharT,SizeT,RefCountT>( data, length );
		}
		
	};
	
	//typedef ManagedDocument<SAMLByte, XMLSize_t> SAMLDocument;
    //typedef ManagedDocument<SAMLByte, long> SAMLDocument;
	typedef ManagedDocument<SAMLByte, size_t> SAMLDocument;
	
}

#endif /*SAMLDOCUMENT_H_*/

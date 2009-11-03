#include <iostream>
#include <speptest/MockWeb.h>

#include <sys/types.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <microhttpd.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#define STR_EXPAND(tok) #tok
#define STR(tok) STR_EXPAND(tok)

using namespace std;

namespace speptest {
	class PostedDocument {
		public:
		PostedDocument() : data(NULL), length(0) {}
		~PostedDocument() { delete[] data; }
		char* data;
		size_t length;

		void append(const char *buf, size_t len){
			char *temp = new char[length+len];
			if (length && data) {
				memcpy(temp, data, length);
				delete[] data;
			}
			memcpy(&temp[length], buf, len);
			data = temp;
			length += len;
		}
	};

	// Callback function for a MHD request
	static int mockWebStaticDispatch(void *cls, struct MHD_Connection *connection,
			const char *url, const char *method, const char *version,
			const char *uploadData, size_t *uploadDataSize,
			void **ptr) {
		MockWeb* mockWeb = static_cast<MockWeb*>(cls);
		PostedDocument* document = static_cast<PostedDocument*>(*ptr);

		// No existing document object - new request
		if (document == NULL) {
			document = new PostedDocument();
			*ptr = static_cast<void*>(document);
			return MHD_YES;
		}

		// A chunk of uploaded data. Buffer it.
		if (*uploadDataSize) {
			document->append(uploadData, *uploadDataSize);
			*uploadDataSize = 0;
			return MHD_YES;
		} else {
			// Done, respond to the request.
			MockWeb::Request req;
			req.url = url;
			req.method = method;
			req.version = version;
			req.uploadData = document->data;
			req.uploadDataSize = document->length;

			MockWeb::Response resp;

			int status = mockWeb->dispatch(req, resp);

			if (resp.document.length()) {
				struct MHD_Response* response = 
					MHD_create_response_from_data(resp.document.length(),
						const_cast<char*>(resp.document.c_str()),
						MHD_NO, MHD_NO);

				MHD_queue_response(connection, status, response);
				MHD_destroy_response(response);
			} else {
				// Send empty document with a 500 error, if there is no response document.
				struct MHD_Response* response = 
					MHD_create_response_from_data(0, const_cast<char*>(""), MHD_NO, MHD_NO);

				MHD_queue_response(connection, 500, response);
				MHD_destroy_response(response);
			}
		}
	}

	// Cleanup callback
	static int mockWebRequestCompleted(void *cls, struct MHD_Connection *connection,
			void **ptr, enum MHD_RequestTerminationCode toe) {
		PostedDocument* document = static_cast<PostedDocument*>(*ptr);
		if (document) delete document;
	}

	int mockWebDefaultHook(MockWeb::Request& req, MockWeb::Response& resp) {
		resp.document = "<html><head><title>404 - Not Found</title></head><body><h3>The requested URL could not be found.</h3></body></html>";
		resp.headers.insert(make_pair("Content-Type", "text/html"));
		return 404;
	}

	MockWeb::MockWeb()
	:
	_baseURL("http://localhost:" STR(MOCKWEB_PORT))
	{
		_daemon = MHD_start_daemon(
			// Use select to avoid lots of threads, make things easier to debug.
			MHD_USE_SELECT_INTERNALLY, MOCKWEB_PORT,
			NULL, NULL,
			&mockWebStaticDispatch, this,
			MHD_OPTION_NOTIFY_COMPLETED, &mockWebRequestCompleted, NULL,
			MHD_OPTION_END
		);

		if (!_daemon) std::cerr << "**** Failed to start web server. Port " STR(MOCKWEB_PORT) " already in use?" << std::endl;
	}

	MockWeb::~MockWeb() {
		MHD_stop_daemon(_daemon);
	}

	int MockWeb::dispatch(MockWeb::Request& req, MockWeb::Response& resp) {
		map<string,MockWebHookType>::const_iterator iter = _hooks.find(req.url);
		MockWebHookType hook = NULL;
		if (iter != _hooks.end()) {
			hook = iter->second;
		}
		if (!hook) hook = &mockWebDefaultHook;

		return hook(req, resp);
	}

	void MockWeb::hook(const std::string& path, MockWebHookType function) {
		_hooks.insert(make_pair(path, function));
	}

	const std::string& MockWeb::getBaseURL() {
		return this->_baseURL;
	}
}

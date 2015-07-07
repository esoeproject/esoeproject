Summary : SPEP and dependencies
Name: spep
Version: 0.8.5
Release: 1.el5
Source0: saml2-%{version}.tar.gz
Source1: spep-%{version}.tar.gz
Source2: spepd-%{version}.tar.gz
Source3: modspep-%{version}.tar.gz
Patch0: spep-config-rpm.patch
License: Apache 2.0
Group: Development/Libraries
BuildRoot: /var/tmp/%{name}-root
Prefix: /usr/local/spep
Requires: boost >= 1.33
Requires: xml-security-c >= 1.3.0
Requires: xerces >= 2.7.0
Requires: libicu >= 3.6
#BuildRequires: boost-devel >= 1.33
#BuildRequires: xml-security-c-devel >= 1.3.0
#BuildRequires: xerces-devel >= 2.7.0
#BuildRequires: libicu-devel >= 3.6
#BuildRequires: xsd = 3.2.0

%description
The SAML2 C++ Library provides an API for manipulating SAML 2.0 compliant
XML documents, and their digital signatures.

The SPEP Library implements this SAML2 library to provide a SAML 2.0
compliant service provider, with custom extensions to support the LXACML
protocol for web content authorization.

This package includes the required files for an Apache SPEP.


%package module
Summary: SPEP Apache module
Group: Development/Libraries
Requires: libapreq2 = 2.13
Requires: httpd >= 2.0.0
#BuildRequires: libapreq2-devel = 2.13
#BuildRequires: httpd-devel >= 2.0.0

%description module
Module for packages Apache version.


%package devel
Summary: SPEP development headers
Group: Development/Libraries

%description devel
Includes headers for developing against the SAML2 and SPEP libraries


%prep
[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n src -c -T -a 0 -a 1 -a 2 -a 3
#%patch0 

%build

cd $RPM_BUILD_DIR/src/saml2cpp

export CXXFLAGS="$RPM_OPT_FLAGS"

./configure --prefix=%{prefix} 
make

export SAML2LIB=-L`find $RPM_BUILD_DIR/src/saml2cpp -name libesoesaml2.so -exec dirname {} \; | head -1`

cd $RPM_BUILD_DIR/src/spepcpp

export CXXFLAGS="$CXXFLAGS -I$RPM_BUILD_DIR/src/saml2cpp/include"
export LDFLAGS="$SAML2LIB"

./configure --prefix=%{prefix} 
make

export SPEPLIB=-L`find $RPM_BUILD_DIR/src/spepcpp -name libspep.so -exec dirname {} \; | head -1`
export CXXFLAGS="$CXXFLAGS -I$RPM_BUILD_DIR/src/spepcpp/include" 
export LDFLAGS="$SAML2LIB $SPEPLIB"

cd $RPM_BUILD_DIR/src/spepcppdaemon

./configure --prefix=%{prefix} 
make

cd $RPM_BUILD_DIR/src/modspep

export APRCONFIG=`which /usr/bin/apr-1-config`
[ -z "$APRCONFIG" ] && echo "couldn't find apr-1-config" && exit 1
export CXXFLAGS="$CXXFLAGS `$APRCONFIG --includes`" 
./configure --prefix=%{prefix} --with-apache2=/usr 
make

%install
rm -rf $RPM_BUILD_ROOT
for Z in saml2cpp spepcpp spepcppdaemon modspep; do
cd $RPM_BUILD_DIR/src/$Z || exit 1
pwd
make install DESTDIR=$RPM_BUILD_ROOT
done
mkdir -p $RPM_BUILD_ROOT/etc/init.d $RPM_BUILD_ROOT/etc/ld.so.conf.d $RPM_BUILD_ROOT/etc/logrotate.d
cat $RPM_BUILD_DIR/src/spepcpp/spepd-initscript | sed '/^SPEP_HOME/ s!\${SPEP_HOME}!'%{prefix}'!;/^log/ s!\$SPEP_HOME/logs!/var/log/spepd!;/^pidfile/ s!\$SPEP_HOME!/var/run/spepd!' > $RPM_BUILD_ROOT/etc/init.d/spepd
chmod +x $RPM_BUILD_ROOT/etc/init.d/spepd
echo %{prefix}/lib > $RPM_BUILD_ROOT/etc/ld.so.conf.d/spep.conf
for Z in '/var/log/spepd/spepd.log {' '	compress' '	copytruncate' '	missingok' '}'; do
echo $Z >> $RPM_BUILD_ROOT/etc/logrotate.d/spepd
done
mkdir -p $RPM_BUILD_ROOT/var/log/spepd
mkdir -p $RPM_BUILD_ROOT/var/run/spepd

%pre
useradd -r spepd

%post
ldconfig
/usr/sbin/sysv-rc-conf spepd on

%postun
userdel spepd

%files
%defattr(-,root,root)

/usr/local/spep/lib/lib*
/usr/local/spep/sbin/spepd
/usr/local/spep/share/saml2
/usr/local/spep/bin
/etc/ld.so.conf.d/spep.conf
/etc/init.d/spepd
/etc/logrotate.d/spepd

%attr(0640,root,spepd) %config /usr/local/spep/etc/spep/spep.conf.default
%attr(0750,root,spepd) %dir /usr/local/spep/etc/spep

%attr(0700,spepd,spepd) %dir /var/log/spepd
%attr(0700,spepd,spepd) %dir /var/run/spepd

%files devel
%defattr(-,root,root)

/usr/local/spep/include/saml2-%{version}
/usr/local/spep/include/spep-%{version}

%files module
%defattr(-,root,root)

/usr/local/spep/lib/modspep.*


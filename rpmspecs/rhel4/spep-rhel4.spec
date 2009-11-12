Summary: SPEP and dependencies
Name: spep
Version: 0.7
Release: 3.el4
Source0: saml2-%{version}.tar.gz
Source1: spep-%{version}.tar.gz
Source2: spepd-%{version}.tar.gz
Source3: modspep-%{version}.tar.gz
Patch0: spep-config-0.7-rpm.patch
License: Apache 2.0
Group: Development/Libraries
BuildRoot: /var/tmp/%{name}-root
Prefix: /usr/local/spep
Requires: boost-ESOE >= 1.33
Requires: xml-security-c-ESOE >= 1.3.0
Requires: xerces-ESOE >= 2.7.0
Requires: icu-ESOE >= 3.6
BuildRequires: boost-ESOE-devel >= 1.33
BuildRequires: xml-security-c-ESOE-devel >= 1.3.0
BuildRequires: xerces-ESOE-devel >= 2.7.0
BuildRequires: icu-ESOE-devel >= 3.6
BuildRequires: xsd = 3.2.0

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
Requires: libapreq2-ESOE >= 2.08
Requires: httpd >= 2.0.0
BuildRequires: libapreq2-ESOE-devel >= 2.08
BuildRequires: httpd-devel >= 2.0.0

%description module
Module for packages Apache version.


%package devel
Summary: SPEP development headers
Group: Development/Libraries

%description devel
Includes headers for developing against the SAML2 and SPEP libraries


%prep
[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n src -c -a 1 -a 2 -a 3
%patch0

%build

cd $RPM_BUILD_DIR/src/saml2-%{version}

export CXXFLAGS="$RPM_OPT_FLAGS"

./configure --prefix=%{prefix} --with-xerces=/usr/local/spep
make

export SAML2LIB=-L`find $RPM_BUILD_DIR/src/saml2-%{version} -name libsaml2.so -exec dirname {} \; | head -1`

cd $RPM_BUILD_DIR/src/spep-%{version}

export CXXFLAGS="$CXXFLAGS -I$RPM_BUILD_DIR/src/saml2-%{version}/include"
export LDFLAGS="$SAML2LIB"

./configure --prefix=%{prefix} --with-xerces=/usr/local/spep
make

export SPEPLIB=-L`find $RPM_BUILD_DIR/src/spep-%{version} -name libspep.so -exec dirname {} \; | head -1`
export CXXFLAGS="$CXXFLAGS -I$RPM_BUILD_DIR/src/spep-%{version}/include"
export LDFLAGS="$SAML2LIB $SPEPLIB"

cd $RPM_BUILD_DIR/src/spepd-%{version}

./configure --prefix=%{prefix} --with-boost-suffix=-mt-esoe --with-xerces=/usr/local/spep
make

cd $RPM_BUILD_DIR/src/modspep-%{version}

export APXS=`which /usr/bin/apxs /usr/bin/apxs2 /usr/sbin/apxs /usr/sbin/apxs2`
[ -z "$APXS" ] && echo "couldn't find apxs" && exit 1
export APRCONFIG=`which /usr/bin/apr-config`
[ -z "$APRCONFIG" ] && echo "couldn't find apr-config" && exit 1
export CXXFLAGS="$CXXFLAGS -I`$APXS -q INCLUDEDIR` `$APXS -q CFLAGS` `$APRCONFIG --includes`"
./configure --prefix=%{prefix} --with-apache2=/usr --with-boost-suffix=-mt-esoe --with-xerces=/usr/local/spep
make

%install
rm -rf $RPM_BUILD_ROOT
for Z in saml2-%{version} spep-%{version} spepd-%{version} modspep-%{version}; do
cd $RPM_BUILD_DIR/src/$Z || exit 1
pwd
make install DESTDIR=$RPM_BUILD_ROOT
done
mkdir -p $RPM_BUILD_ROOT/etc/init.d $RPM_BUILD_ROOT/etc/ld.so.conf.d $RPM_BUILD_ROOT/etc/logrotate.d
cat $RPM_BUILD_DIR/src/spep-%{version}/spepd-initscript | sed '/^SPEP_HOME/ s!\${SPEP_HOME}!'%{prefix}\! > $RPM_BUILD_ROOT/etc/init.d/spepd
chmod +x $RPM_BUILD_ROOT/etc/init.d/spepd
echo %{prefix}/lib > $RPM_BUILD_ROOT/etc/ld.so.conf.d/spep.conf
for Z in '/usr/local/spep/logs/spepd.log {' '	compress' '	copytruncate' '	missingok' '}'; do
echo $Z >> $RPM_BUILD_ROOT/etc/logrotate.d/spepd
done

%post
ldconfig
/sbin/chkconfig --add spepd

%files
%defattr(-,root,root)

/usr/local/spep/etc/spep/spep.conf.default
/usr/local/spep/lib/lib*
/usr/local/spep/sbin/spepd
/usr/local/spep/share/saml2-%{version}
/usr/local/spep/bin
/etc/ld.so.conf.d/spep.conf
/etc/init.d/spepd
/etc/logrotate.d/spepd

%files devel
%defattr(-,root,root)

/usr/local/spep/include/saml2-%{version}
/usr/local/spep/include/spep-%{version}

%files module
%defattr(-,root,root)

/usr/local/spep/lib/modspep.*


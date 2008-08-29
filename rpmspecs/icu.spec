Summary: International Components for Unicode
Name: icu-ESOE
Version: 3.8.1
Release: 1
Source0: icu4c-3_8_1-src.tgz
License: Apache 2.0
Group: Development/Libraries
BuildRoot: /var/tmp/%{name}-root
Prefix: /usr/local/spep

%package devel
Summary: International Components for Unicode headers
Group: Development/Libraries

%description
ICU is the premier library for software internationalization

%description devel
Header files for ICU

%prep
[ $UID -eq 0 ] && echo "rpmbuild as root is bad." >&2 && exit 1
%setup -q -n icu/source

%build
./configure --prefix=%{prefix}
make

%install
rm -rf $RPM_BUILD_ROOT
make install DESTDIR="$RPM_BUILD_ROOT"
mv $RPM_BUILD_ROOT/usr/local/spep/man $RPM_BUILD_ROOT/usr/local/spep/share/

%post
/sbin/ldconfig $RPM_INSTALL_PREFIX0/lib

%postun
/sbin/ldconfig

%files
%defattr(-,root,root)

/usr/local/spep/lib/libicudata.so.38.1
/usr/local/spep/lib/libicule.so.38.1
/usr/local/spep/lib/libicuuc.so.38.1
/usr/local/spep/lib/libicuio.so.38.1
/usr/local/spep/lib/libicutu.so.38.1
/usr/local/spep/lib/libiculx.so.38.1
/usr/local/spep/lib/libicui18n.so.38.1
/usr/local/spep/lib/libicudata.so.38
/usr/local/spep/lib/libicule.so.38
/usr/local/spep/lib/libicuuc.so.38
/usr/local/spep/lib/libicuio.so.38
/usr/local/spep/lib/libicutu.so.38
/usr/local/spep/lib/libiculx.so.38
/usr/local/spep/lib/libicui18n.so.38
/usr/local/spep/lib/libicudata.so
/usr/local/spep/lib/libicule.so
/usr/local/spep/lib/libicuuc.so
/usr/local/spep/lib/libicuio.so
/usr/local/spep/lib/libicutu.so
/usr/local/spep/lib/libiculx.so
/usr/local/spep/lib/libicui18n.so
/usr/local/spep/bin/makeconv
/usr/local/spep/bin/genbrk
/usr/local/spep/bin/genctd
/usr/local/spep/bin/pkgdata
/usr/local/spep/bin/icu-config
/usr/local/spep/bin/derb
/usr/local/spep/bin/genrb
/usr/local/spep/bin/uconv
/usr/local/spep/bin/gencnval
/usr/local/spep/sbin/genuca
/usr/local/spep/sbin/icuswap
/usr/local/spep/sbin/genccode
/usr/local/spep/sbin/gencmn
/usr/local/spep/sbin/icupkg
/usr/local/spep/sbin/gensprep
/usr/local/spep/share/icu

%files devel
%defattr(-,root,root)

/usr/local/spep/share/man
/usr/local/spep/include/unicode
/usr/local/spep/include/layout
/usr/local/spep/lib/icu


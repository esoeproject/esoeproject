Summary: International Components for Unicode
Name: icu-ESOE
Version: 4.2.1
Release: 2.el4
Source0: icu4c-4_2_1-src.tgz
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

%post
/sbin/ldconfig $RPM_INSTALL_PREFIX0/lib

%postun
/sbin/ldconfig

%files
%defattr(-,root,root)

/usr/local/spep/lib/libicu*
/usr/local/spep/bin
/usr/local/spep/sbin
/usr/local/spep/share/icu

%files devel
%defattr(-,root,root)

/usr/local/spep/share/man
/usr/local/spep/include/unicode
/usr/local/spep/include/layout
/usr/local/spep/lib/icu


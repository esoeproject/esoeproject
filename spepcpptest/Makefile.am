
AUTOMAKE_OPTIONS = foreign
SUBDIRS = include src 

EXTRA_DIST = AUTHORS LICENSE NOTICE README

data/keystore.ks: data/keytool-responses.txt
	rm -f $@
	$(KEYTOOL) -genkey -keystore $@ -storepass testks -alias spep -keypass testkey -keyalg rsa -keysize 512 < data/keytool-responses.txt

schema: ../saml2cpp/schema
	mkdir -p schema
	cp ../saml2cpp/schema/*.xsd schema/

pre_test: all schema data/keystore.ks

test: pre_test
	src/spepcpptest$(EXEEXT)

debug: pre_test
	gdb src/spepcpptest$(EXEEXT)

logs:
	mkdir -p logs

valgrind: pre_test logs
	$(VALGRIND) --track-origins=yes --leak-check=full --log-file=logs/test-%p.log --show-reachable=yes src/spepcpptest$(EXEEXT)

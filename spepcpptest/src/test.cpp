#include <string>
#include <iostream>
#include <gtest/gtest.h>

#include <speptest/MockWeb.h>

#include <curl/curl.h>

using namespace speptest;
using namespace std;

static const char *testWebHookDocument = "Test document";
static int testWebHook(MockWeb::Request& req, MockWeb::Response &resp) {
	resp.document = testWebHookDocument;
	resp.headers.insert(make_pair("Content-Type", "text/html"));
	return 200;
}

static int curlTestCallback(void *buffer, size_t size, size_t nmemb, void *userp) {
	char** target = reinterpret_cast<char**>(userp);
	if (*target) delete[] *target;
	*target = new char[size*nmemb+1];
	memcpy(*target, buffer, size*nmemb);
	(*target)[size*nmemb] = '\0';

	return size*nmemb;
}

TEST(MockWebTest, Serve) {
	speptest::MockWeb mockWeb;

	std::string path("/test");
	mockWeb.hook(path, &testWebHook);

	CURL* curl = curl_easy_init();
	std::string url = mockWeb.getBaseURL() + path;
	curl_easy_setopt(curl, CURLOPT_URL, url.c_str());

	char *buf = NULL;
	curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &curlTestCallback);
	curl_easy_setopt(curl, CURLOPT_WRITEDATA, &buf);

	curl_easy_perform(curl);

	curl_easy_cleanup(curl);

	ASSERT_TRUE(NULL != buf);
	ASSERT_STREQ(testWebHookDocument, buf);
}

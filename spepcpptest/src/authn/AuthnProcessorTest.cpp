#include <gtest/gtest.h>

#include <speptest/GlobalFixtures.h>

#include <spep/authn/AuthnProcessor.h>

using namespace spep;

namespace speptest {
	class AuthnProcessorTest : public ::testing::Test, protected ::speptest::GlobalFixtures {
	};

	TEST_F(AuthnProcessorTest, GenerateAuthnRequest) {
		AuthnProcessorData data;
		data.setBaseRequestURL(serviceHost);
		authnProcessor->generateAuthnRequest(data);

		ASSERT_FALSE(NULL == data.getRequestDocument().getData());
	}
}

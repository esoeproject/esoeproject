#include <gtest/gtest.h>

#include <speptest/GlobalFixtures.h>

#include <spep/authn/AuthnProcessor.h>

using namespace spep;

namespace speptest {
	class AuthnProcessorTest : public ::testing::Test, protected ::speptest::GlobalFixtures {
		protected:

		virtual void SetUp() {
		}

		virtual void TearDown() {
		}
	};

	TEST_F(AuthnProcessorTest, GenerateAuthnRequest) {
		AuthnProcessorData data;
		authnProcessor->generateAuthnRequest(data);
	}
}

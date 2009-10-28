#include <string>
#include <gtest/gtest.h>

TEST(ExampleTest, String) {
	std::string str("abcdef");

	EXPECT_EQ(0, str.find("a"));
	EXPECT_EQ(std::string::npos, str.find("g"));
}

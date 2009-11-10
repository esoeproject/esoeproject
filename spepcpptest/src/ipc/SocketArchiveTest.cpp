#include <spep/ipc/SocketArchive.h>
#include <spep/Util.h>
#include <spep/UnicodeStringConversion.h>

#include <queue>
#include <iostream>

#include <gtest/gtest.h>

#include <boost/bind.hpp>


using namespace boost;
using namespace spep::ipc;

class SocketArchiveTest : public ::testing::Test {
	public:
	SocketArchiveTest()
	:
	queue(),
	sa(bind(&SocketArchiveTest::write, this, _1), bind(&SocketArchiveTest::read, this, _1))
	{}

	std::queue< std::vector<char> > queue;
	SocketArchive sa;

	void write(const std::vector<char>& buffer) {
		queue.push(buffer);
	}
	void read(std::vector<char>& buffer) {
		while (queue.empty()) spep::delay(0, 50);
		buffer.swap(queue.front());
		queue.pop();
	}

	template <class T>
	void testType(T value) {
		T expected(value);
		sa.out() << expected;

		T result;
		sa.in() >> result;

		ASSERT_TRUE(expected == result);
	}
};

TEST_F(SocketArchiveTest, StringTest) {
	testType<std::string>(std::string("This is a test message."));
}

TEST_F(SocketArchiveTest, WStringTest) {
	testType<std::wstring>(std::wstring(L"This is a wide test message."));
}

TEST_F(SocketArchiveTest, UnicodeStringTest) {
	UnicodeString ustring( spep::UnicodeStringConversion::toUnicodeString("This is a unicode test message.\\U0012872f") );
	testType<UnicodeString>(ustring.unescape());
}

TEST_F(SocketArchiveTest, IntTest) {
	testType<int>(123456);
}

TEST_F(SocketArchiveTest, LongTest) {
	testType<long>(1234567890L);
}

TEST_F(SocketArchiveTest, ShortTest) {
	testType<short>(1234);
}

TEST_F(SocketArchiveTest, FloatTest) {
	testType<float>(1.5f);
}

TEST_F(SocketArchiveTest, DoubleTest) {
	testType<double>(1.500005781l);
}

TEST_F(SocketArchiveTest, VectorTest) {
	std::vector<std::string> value;
	value.push_back(std::string("1. This is a test message."));
	value.push_back(std::string("2. This is a test message."));
	value.push_back(std::string("3. This is a test message."));
	value.push_back(std::string("4. This is a test message."));
	value.push_back(std::string("5. This is a test message."));
	value.push_back(std::string("6. This is a test message."));
	value.push_back(std::string("7. This is a test message."));
	value.push_back(std::string("8. This is a test message."));

	testType<std::vector<std::string> >(value);
}

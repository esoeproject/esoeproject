#include <saml2/logging/Logger.h>
#include <spep/ipc/Socket.h>
#include <spep/ipc/Dispatcher.h>
#include <spep/Util.h>

#include <boost/thread.hpp>
#include <boost/function.hpp>
#include <boost/bind.hpp>

#include <asio.hpp>

#include <gtest/gtest.h>

using namespace boost;
using asio::ip::tcp;
using namespace spep::ipc;

class SocketTest : public ::testing::Test, public Dispatcher {
	public:
	struct terminate{};
	class RequestObject {
		public:
		std::string value;
		int number;

		template <class Archive>
		void serialize(Archive &ar, const unsigned int version) {
			ar & value & number;
		}
	};
	class ResponseObject {
		public:
		int number;
		std::string value;

		template <class Archive>
		void serialize(Archive &ar, const unsigned int version) {
			ar & number & value;
		}
	};

	SocketTest()
	:
	logger(),
	port(17142),
	serverSocket(&logger, this, port),
	clientSocketPool(port, 1),
	clientSocket(clientSocketPool.get()),
	thread(bind(&SocketTest::runServer, this))
	{
	}

	virtual ~SocketTest() {
	}

	saml2::Logger logger;
	int port;
	ServerSocket<SocketTest> serverSocket;
	ClientSocketPool clientSocketPool;
	ClientSocket* clientSocket;
	boost::thread thread;

	MessageHeader header;
	RequestObject request;
	ResponseObject response;

	virtual bool dispatch(MessageHeader &header, Engine &en) {
		this->header = header;
		en.getObject(request);

		en.sendResponseHeader();
		en.sendObject(response);

		return true;
	}

	void runServer() {
		try {
			serverSocket.listen();
		} catch (terminate& t) {
		}
	}
};

TEST_F(SocketTest, Test) {
	RequestObject req;
	req.value = std::string("This is a test value");
	req.number = 1234;

	response.value = std::string("This is the response object");
	response.number = 4321;

	std::string dispatch("test");
	ResponseObject res = clientSocket->makeRequest<ResponseObject>(dispatch, req);

	ASSERT_EQ(response.value, res.value);
	ASSERT_EQ(response.number, res.number);
	ASSERT_EQ(req.value, request.value);
	ASSERT_EQ(req.number, request.number);
}

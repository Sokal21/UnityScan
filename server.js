const http = require("http");
const { StringDecoder } = require("string_decoder");
const { HTTP_PORT } = require("./constants");

const server = http.createServer((req, res) => {
  if (req.method === "POST") {
    const decoder = new StringDecoder("utf-8");
    let buffer = "";

    req.on("data", (data) => {
      buffer += decoder.write(data);
    });

    req.on("end", () => {
      buffer += decoder.end();
      console.log(buffer); // Print the body of the POST request to the console

      res.writeHead(200, { "Content-Type": "text/plain" });
      res.end("Received POST request\n");
    });
  } else {
    res.writeHead(405, { "Content-Type": "text/plain" });
    res.end("Method Not Allowed\n");
  }
});

server.listen(HTTP_PORT, () => {
  console.log(`Server running on port ${HTTP_PORT}`);
});
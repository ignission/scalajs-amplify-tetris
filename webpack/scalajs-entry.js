if (process.env.NODE_ENV === "production") {
  const opt = require("./tetris-opt.js");
  opt.main();
  module.exports = opt;
} else {
  var exports = window;
  exports.require = require("./tetris-fastopt-entrypoint.js").require;
  window.global = window;

  const fastOpt = require("./tetris-fastopt.js");
  fastOpt.main();
  module.exports = fastOpt;

  if (module.hot) {
    module.hot.accept();
  }
}

React = require("react");
var classreg  = require('../out/inductive');

/*
 * GET home page.
 */

console.log(classreg);

exports.index = function(req, res){
  res.render('index', { title: 'Express', content: classreg.getContentHtml() });
};

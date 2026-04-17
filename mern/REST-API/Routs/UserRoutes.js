// const experess = require("express");
// const Router = experess.Router();
import experess from "express";
const Router = experess.Router();

// const { Authmiddleware } = require("../Middleware/AuthMiddleware.js");
// const {
//   registerUser,
//   loginUser,
//   getUser,
// } = require("../Controller/UserController.js");

import { Authmiddleware } from "../Middleware/AuthMiddleware.js";
import {
  registerUser,
  loginUser,
  getUser,
} from "../Controller/UserController.js";

Router.post("/", registerUser);
Router.post("/login", loginUser);
Router.get("/me", Authmiddleware, getUser);

export default Router;
// module.exports = Router;

// const bcript = require("bcryptjs");
// const jwt = require("jsonwebtoken");
// const User = require("../Models/UserModel");
// const asyncHandler = require("express-async-handler");

import bcript from "bcryptjs";
import jwt from "jsonwebtoken";
import User from "../Models/UserModel.js";
import asyncHandler from "express-async-handler";

//@post /user
// Create a new user
//acces public
// Function to handle user creation

const registerUser = asyncHandler(async (req, res) => {
  const { name, email, password } = req.body;
  if (!name || !email || !password) {
    res.status(500);
    throw new Error("fill the all the fiields");
  }
  const salt = await bcript.genSalt(10);
  const encriptedPass = await bcript.hash(password, salt);
  const user = await User.create({
    name: name,
    email: email,
    password: encriptedPass,
  });
  if (user) {
    res.status(201).json({
      _id: user.id,
      name: user.name,
      email: user.email,
      Token: TokenGenerate(user.id),
    });
  } else {
    res.status(500);
    throw new Error("some thing went wrong");
  }
});

// Middleware to validate user input
// This can be expanded based on requirements
// For example, checking if email is valid, password strength, etc.
// Here is a simple example:/
const loginUser = asyncHandler(async (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ error: "All fields are required." });
  }
  const user = await User.findOne({ email: email });
  if (user && (await bcript.compare(password, user.password))) {
    // set cookie headers before sending the response body
    res
      .cookie("token", TokenGenerate(user.id), {
        httpOnly: true,
        secure: false,
        sameSite: "lax",
        maxAge: 24 * 60 * 60 * 1000,
      })
      .status(200)
      .json({
        _id: user.id,
        name: user.name,
        email: user.email,
        Token: TokenGenerate(user.id),
      });
    return;
  }
  res.status(401);
  throw new Error("Invalid credentials");
});

// Main controller function to create a user
// This function uses the loginUser middleware for validation/
// and then proceeds to create the user./
// In a real application, you would also hash the password/

const getUser = asyncHandler(async (req, res) => {
  const user = req.user;
  if (!user) {
    res.status(401);
    throw new Error("user not Autherized");
  }

  return res.status(200).json({
    _id: user.id,
    name: user.name,
    email: user.email,
  });
});

const TokenGenerate = (id) => {
  return jwt.sign({ id }, process.env.SECRET_TOKEN, { expiresIn: "30d" });
};

export { registerUser, loginUser, getUser };

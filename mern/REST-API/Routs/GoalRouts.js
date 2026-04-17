// const express = require("express");
// const router = express.Router();
import express from "express";
const router = express.Router();
import {
  getGoals,
  setGoals,
  updateGoals,
  deleteGoals,
} from "../Controller/Controller.js";

// const {
//   getGoals,
//   setGoals,
//   updateGoals,
//   deleteGoals,
// } = require("../Controller/Controller.js");

router.get("/", getGoals).post("/", setGoals);
router.put("/:id", updateGoals).delete("/:id", deleteGoals);

export default router;
// module.exports = router;

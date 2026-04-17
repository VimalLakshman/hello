import asyncHandler from "express-async-handler";
import GoalModel from "../Models/GoalModel.js";
//@ access private
//@ route GET /api/goals
//@ desc  Get goals
const getGoals = asyncHandler(async (req, res) => {
  const goals = await GoalModel.find();

  res.status(200).json(goals);
});

//@ access private
//@ route POST /api/goals
//@ desc  Set goals
const setGoals = asyncHandler(async (req, res) => {
  if (!req.body || !req.body.text) {
    res.status(400);
    throw new Error("Please add a text field");
  }

  const goal = await GoalModel.create({ text: req.body.text });
  res.status(201).json(goal);
});

//@ access private
//@ route DELETE /api/goals/:id
//@ desc  Delete goals
const deleteGoals = async (req, res) => {
  const goal = await GoalModel.findByIdAndDelete(req.params.id);
  if (!goal) {
    res.status(400);
    throw new Error("Goal not found");
  }

  res.status(200).json({ message: `Delete goal ${req.params.id}` });
};

//@ access private
//@ route PUT /api/goals/:id
//@ desc  Update goals

const updateGoals = async (req, res) => {
  const goal = await GoalModel.findByIdAndUpdate(req.params.id, req.body, {
    runValidators: true,
  });
  if (!goal) {
    res.status(400);
    throw new Error("Goal not found");
  }

  res.status(200).json({ message: goal });
};

// module.exports = {
//   getGoals,
//   setGoals,
//   deleteGoals,
//   updateGoals,
// };

export { getGoals, setGoals, deleteGoals, updateGoals };

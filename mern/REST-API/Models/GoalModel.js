import mongoose from "mongoose";

const GoalSchema = new mongoose.Schema(
  {
    user: { type: mongoose.Schema.Types.ObjectId, ref: "User", required: true },
  },
  {
    text: { type: String, required: true },
  },
  { timestamps: true }
);

// module.exports =
//   mongoose.models.UserGoal || mongoose.model("UserGoal", GoalSchema);
export default mongoose.models.UserGoal ||
  mongoose.model("UserGoal", GoalSchema);

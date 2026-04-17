import mongoose from "mongoose";

const User = new mongoose.Schema(
  {
    name: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },
  },
  {
    timestamps: true,
  }
);

// module.exports = mongoose.models.User || mongoose.model("User", User);
export default mongoose.models.User || mongoose.model("User", User);

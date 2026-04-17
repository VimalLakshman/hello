import express from "express";
import dotenv from "dotenv";
const port = process.env.PORT || 5000;
import { errorHandler } from "./Middleware/Middleware.js";
dotenv.config();
import connectDB from "./Config/db.js";
import goalRoutes from "./Routs/GoalRouts.js";
import userRoutes from "./Routs/UserRoutes.js";

const app = express();
connectDB();
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use("/goals", goalRoutes);
app.use("/user", userRoutes);
app.use(errorHandler);
app.listen(port, () => {
  console.log("the server is running on port " + port);
});

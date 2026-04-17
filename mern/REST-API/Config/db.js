import mongoose from "mongoose";
import colors from "colors";

const connectDB = async () => {
  try {
    const con = await mongoose.connect(process.env.MONGO_URI);
    console.log(`MongoDB connected: ${con.connection.host}`.green.bold);
  } catch (e) {
    console.error(`Error: ${e.message}`);
    process.exit(1);
  }
};

// module.exports = connectDB;
export default connectDB;

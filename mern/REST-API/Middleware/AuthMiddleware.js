import jwt from "jsonwebtoken";
import User from "../Models/UserModel.js";

const Authmiddleware = async (req, res, next) => {
  let Token;

  try {
    if (
      req.headers.authorization &&
      req.headers.authorization.startsWith("Bearer")
    ) {
      Token = req.headers.authorization.split(" ")[1];
      const decodeId = jwt.verify(Token, process.env.SECRET_TOKEN);
      const user = await User.findById(decodeId.id).select("-password");
      if (user) {
        req.user = user;
        return next();
      } else {
        res.status(401);
        throw new Error("unAutherized");
      }
    }
    if (!Token) {
      res.status(401);
      throw new Error("unAutherized");
    }
  } catch (error) {
    res.status(401);
    throw new Error("unAutherized");
  }
};

export { Authmiddleware };

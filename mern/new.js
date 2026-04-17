let a = "Apple Bannana cherry Vimal lokesh thevidiya";

let b = a.split(" ").filter((a) => {
  return a[0] === a[0].toUpperCase();
});
console.log(b);

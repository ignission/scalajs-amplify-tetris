import upickle.default._

case class PackageJson(
    dependencies: Seq[(String, String)],
    devDependencies: Seq[(String, String)]
)

object PackageJson {
  implicit val r: Reader[PackageJson] = JsObjR.map { obj =>
    PackageJson(
      dependencies = readDeps(obj, "dependencies"),
      devDependencies = readDeps(obj, "devDependencies")
    )
  }

  private def readDeps(obj: ujson.Obj, key: String) =
    obj(key).obj.map { case (k, v) => k -> v.str }.toSeq

  def readFrom(readable: ujson.Readable): PackageJson =
    read[PackageJson](readable)
}

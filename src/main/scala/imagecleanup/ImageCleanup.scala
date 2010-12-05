package imagecleanup

import java.io.{FileFilter, File}

object ImageCleanup {
  val ignoredFiles = List("Thumbs.db", ".DS_Store", ".localized", ".picasa.ini")
  val ignoredDirs = List(".picasaoriginals")

  def main(args: Array[String]) {
    val root = new File("/Users/eea/Pictures")
    checkFolder(root)
  }

  def checkFolder(dir: File) {
    val filesDirs = dir.listFiles.toList.partition(_.isFile)
    val files = filesDirs._1.map(_.getName).filter(!ignoredFiles.contains(_))
    val dirs = filesDirs._2.map(_.getName).filter(!ignoredDirs.contains(_))
    if (!files.isEmpty && !dirs.isEmpty) {
      System.err.println("DIR:"+dir.getAbsolutePath)
      println(pretty(files.take(5)))
      System.err.println(pretty(dirs.take(5)))
    }
    filesDirs._2.foreach(checkFolder)
  }

  def pretty(files: List[String]) = files.mkString("\n")
}


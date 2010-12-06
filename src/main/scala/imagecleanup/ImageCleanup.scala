package imagecleanup

import java.io.{FileFilter, File}
import javax.imageio.ImageIO

object ImageCleanup {
  val ignoredFiles = List("Thumbs.db", ".DS_Store", ".localized", ".picasa.ini")
  val ignoredDirs = List(".picasaoriginals")
  val extensions = List("jpg", "png", "jpeg", "tiff", "gif", "bmp")
  val temp = new File(System.getProperty("tempDir"))

  def main(args: Array[String]) {
    val root = new File(System.getProperty("rootDir"))
    //checkFolder(root)
    moveCorruptedImagesToTemp(root)
  }

  def checkFolder(dir: File) {
    val (allFiles, allDirs) = filesAndDirs(dir)
    val files = allFiles.map(_.getName).filter(!ignoredFiles.contains(_))
    val dirs = filteredDirs(allDirs)
    if (!files.isEmpty && !dirs.isEmpty) {
      System.err.println("DIR:" + dir.getAbsolutePath)
      println(pretty(files.take(5)))
      System.err.println(pretty(dirs.map(_.getName).take(5)))
    }
    allDirs.foreach(checkFolder)
  }

  def pretty(files: List[String]) = files.mkString("\n")

  def moveCorruptedImagesToTemp(dir: File) {
    println(dir.getAbsolutePath)
    val (allFiles, allDirs) = filesAndDirs(dir)
    allFiles.filter(f => extensions.contains(f.getName.split('.').last.toLowerCase)).foreach(moveIfNotOk)
    filteredDirs(allDirs).foreach(moveCorruptedImagesToTemp)
  }

  def moveIfNotOk(img: File) {
    if (!isOk(img)) img.renameTo(new File(temp, img.getName))
  }

  def filesAndDirs(dir: File) = dir.listFiles.toList.partition(_.isFile)

  def isOk(img: File) = try {
    ImageIO.read(img).getWidth > 0
  } catch {
    case e: Exception => false
  }

  def filteredDirs(files: List[File]) = files.filter(f => !ignoredDirs.contains(f.getName))
}


package imagecleanup

import javax.imageio.ImageIO
import java.lang.String
import collection.immutable.List
import java.security.MessageDigest
import java.io.File
import org.apache.commons.io.FileUtils

//Example of VM parameters: -DrootDir=/Users/eea/Pictures -DtempDir=/Users/eea/temp -Xmx512m
object ImageCleanup {
  val ignoredFiles = List("Thumbs.db", ".DS_Store", ".localized", ".picasa.ini")
  val ignoredDirs = List(".picasaoriginals")
  val extensions = List("jpg", "png", "jpeg", "tiff", "gif", "bmp", "nef")
  val priorities = List("DSC", "P", "IM", "FILE", "#")
  val temp = new File(System.getProperty("tempDir"))
  val startTime = System.currentTimeMillis

  def main(args: Array[String]) {
    val root = new File(System.getProperty("rootDir"))
    //moveCorruptedImagesToTemp(root)
    deleteDuplicates(root);
    println(duration.formatted("%4.1f"))
  }

  def duration = (System.currentTimeMillis - startTime) / 1000.0

  def deleteDuplicates(root: File) {
    def safeFileHash: (File) => String = md5SumString(_)
    def fastFileHash: (File) => Long = _.length
    val duplicates = recursiveFiles(root).groupBy(fastFileHash).filter(_._2.size > 1)
    duplicates.map(_._2.sortBy(priority)).foreach(files => {
      println("->" + sizeAndPath(files.first))
      val removableFiles = files.drop(1)
      println(removableFiles.map(sizeAndPath).mkString("\n"))
      //  removableFiles.foreach(moveToTemp)
    })
    println("size: " + duplicates.size)

    def priority(file: File): Int = {
      val index = priorities.findIndexOf(file.getName.toUpperCase.startsWith)
      val pathGrade = if (file.getAbsolutePath.contains("Lajittelematta")) 10 else 0
      pathGrade + (if (index < 0) priorities.size else index)
    }
  }

  def sizeAndPath(file: File) = "" + file.length + " " + file.getAbsolutePath

  def md5SumString(file: File): String = {
    val bytes = FileUtils.readFileToByteArray(file)
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(bytes)
    md5.digest().map(0xFF & _).map("%02x".format(_)).mkString("")
  }

  def recursiveFiles(dir: File): List[File] = {
    val (files, dirs) = dir.listFiles.toList.partition(_.isFile)
    val filteredFiles = files.filter(f => extensions.contains(f.getName.split('.').last.toLowerCase))
    def systemDir: (File) => Boolean = f => ignoredDirs.contains(f.getName)
    filteredFiles ++ dirs.filterNot(systemDir).flatMap(recursiveFiles)
  }

  def moveCorruptedImagesToTemp(dir: File) = recursiveFiles(dir).foreach(moveToTempIfNotOk)

  def moveToTempIfNotOk(img: File) = if (isCorrupted(img)) moveToTemp(img)

  def moveToTemp(file: File) {
    val succeed: Boolean = file.renameTo(new File(temp, file.getName))
    if (!succeed) file.renameTo(new File(temp, "" + duration + "-" + file.getName))
  }

  def isCorrupted(img: File) = try {
    ImageIO.read(img).getWidth <= 0
  } catch {
    case e: Exception => true
  }
}

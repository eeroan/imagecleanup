package imagecleanup

import java.io.{FileFilter, File}
import javax.imageio.ImageIO
import java.lang.String
import collection.immutable.{List, Map}

object ImageCleanup {
  val ignoredFiles = List("Thumbs.db", ".DS_Store", ".localized", ".picasa.ini")
  val ignoredDirs = List(".picasaoriginals")
  val extensions = List("jpg", "png", "jpeg", "tiff", "gif", "bmp", "nef")
  val temp = new File(System.getProperty("tempDir"))

  def main(args: Array[String]) {
    val root = new File(System.getProperty("rootDir"))
    //checkFolder(root)
    //moveCorruptedImagesToTemp(root)
    //renameFilesWithNumberSign(root);
    deleteDuplicates(root);
  }

  def deleteDuplicates(root: File) {
    forEachFolder(root, files => {
      val duplicates: Map[Long, List[File]] = files.groupBy(_.length).filter(_._2.size > 1)
      duplicates.map(_._2).foreach(files => {
        var filtered = files.filter(file => file.getName.startsWith("FILE"))
        //TODO: preserve one file from duplicates
        System.err.println(files.mkString("*"))
        //filtered.foreach(moveToTemp)
      })
    })
  }

  def renameFilesWithNumberSign(root: File) {
    forEachFile(root, file => {
      val name: String = file.getName
      if (name.startsWith("#")) {
        val newName = name.substring(0, 3) match {
          case "#SC" => name.replace("#", "D")
          case "#sc" => name.replace("#", "d")
          case "#MG" => name.replace("#", "I")
          case "#mg" => name.replace("#", "i")
          case "#MA" => name.replace("#", "I")
          case "#ma" => name.replace("#", "i")
          case _ => System.err.println(name); name
        }
        val newFile = new File(file.getParent, newName)
        println(file.getAbsolutePath + " -> " + newFile.getAbsolutePath)
        file.renameTo(newFile)
      }
    })
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

  def moveCorruptedImagesToTemp(dir: File) = forEachFile(dir, file => moveIfNotOk(file))

  def forEachFile(dir: File, action: (File) => Unit) = forEachFolder(dir, files => files.foreach(action))

  def forEachFolder(dir: File, action: (List[File]) => Unit) {
    val (allFiles, allDirs) = filesAndDirs(dir)
    action(allFiles.filter(f => extensions.contains(f.getName.split('.').last.toLowerCase)))
    filteredDirs(allDirs).foreach(forEachFolder(_, action))
  }

  def moveIfNotOk(img: File) = if (!isOk(img)) moveToTemp(img)

  def moveToTemp(file: File) {
    file.renameTo(new File(temp, file.getName))
  }

  def filesAndDirs(dir: File) = dir.listFiles.toList.partition(_.isFile)

  def isOk(img: File) = try {
    ImageIO.read(img).getWidth > 0
  } catch {
    case e: Exception => false
  }

  def filteredDirs(files: List[File]) = files.filter(f => !ignoredDirs.contains(f.getName))
}


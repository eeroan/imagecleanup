package samples

import org.junit.runner.RunWith
import org.specs._
import org.specs.matcher._
import org.specs.runner.{JUnitSuiteRunner, JUnit}

@RunWith(classOf[JUnitSuiteRunner])
class MySpecTest extends Specification with JUnit /*with ScalaCheck*/ {

  "My" should {
    "allow " in {
    }
    "deny " in {
    }
  }

  "A List" should {
    "have a size method returning the number of elements in the list" in {
      List(1, 2, 3).size must_== 3
    }
  }

}

object MySpecMain {
  def main(args: Array[String]) {
    new MySpecTest().main(args)
  }
}

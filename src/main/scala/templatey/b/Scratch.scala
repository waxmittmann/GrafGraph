package templatey.b

object Scratch {


  object Step1 {
    def s1(name: String): Step2 = Step2(name)
  }

  case class Step2(name: String) {

    def x(thing: String): Step4 = Step4(name, Some(thing), Seq.empty)

    def y: Step4 = Step4(name, None, Seq.empty)

  }

//  case class Step3(name: String, other: Option[String]) {
//    def x(thing: String): Step4 = Step4(name, other, Seq(thing))
//
//    def y: Step5 = Step5(name, other, Seq.empty)
//  }

  case class Step4(name: String, other: Option[String], seq: Seq[String]) {
    def x(thing: String): Step4 = this.copy(seq = thing +: seq)

    def y: Step5 = Step5(name, other, seq)
  }

  case class Step5(name: String, other: Option[String], seq: Seq[String])


  def main(args: Array[String]): Unit = {

    val x: Step5 = Step1.s1("hallo") x "whatevs" x "blah" x "blah" x "blah" y

    println(x)

  }
}

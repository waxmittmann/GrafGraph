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

    def y(): Step5 = Step5(name, other, seq, Seq.empty)
  }

  case class Step5(name: String, other: Option[String], seq: Seq[String], seq2: Seq[Int]) {
    def x(thing: Int): Step5 = this.copy(seq2 = thing +: seq2)

//    def y: Step6 = Step6(name, other, seq, seq2)
    def y(): Step6 = Step6(name, other, seq, seq2)
  }

  case class Step6(name: String, other: Option[String], seq: Seq[String], seq2: Seq[Int])


  def main(args: Array[String]): Unit = {

//    val x: Step6 = (Step1.s1("hallo") x "whatevs" x "blah" x "blah" x "blah" y) x 1 x 2 x 3 y
    val x1: Step6 = Step1.s1("hallo") x "whatevs" x "blah" x "blah" x "blah" y() x 1 x 2 y()
    println(x1)

  }
}

package bookings

import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

case class Booking(id: Option[Long],
                   userId: Long,
                   courtNumber: Int,
                   year: Int,
                   month: Int,
                   day: Int,
                   startTime: Int,
                   endTime: Int
                  )

class Bookings(tag: Tag) extends Table[Booking](tag, "bookings") {


  def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId: Rep[Long] = column[Long]("user_id")
  def courtNumber: Rep[Int] = column[Int]("court_number")
  def year: Rep[Int] = column[Int]("year")
  def month: Rep[Int] = column[Int]("month")
  def day: Rep[Int] = column[Int]("day")
  def startTime: Rep[Int] = column[Int]("start_time")
  def endTime: Rep[Int] = column[Int]("end_time")

  def * : ProvenShape[Booking] = (id.?, userId, courtNumber, year, month, day, startTime, endTime) <>(Booking.tupled, Booking.unapply)
}
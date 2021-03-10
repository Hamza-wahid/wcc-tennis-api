package authentication

import akka.actor.{Actor, ActorLogging}
import akka.pattern.pipe
import authentication.AuthUtils.{checkPassword, hashPassword, isDataValid}
import authentication.messages.AuthManagerMessages.{LoginUser, RegisterUser, UserAuthResult}
import authentication.messages.AuthManagerMessages.UserAuthResult._
import authentication.requests.UserAuthRequests._
import core.authorisation.JwtAuthUtils.generateToken

import scala.concurrent.{ExecutionContext, Future}

class UserAuthManager(userDB: UserRepo)(implicit ec: ExecutionContext) extends Actor with ActorLogging {

  override def receive: Receive = {
    case RegisterUser(UserRegistrationRequest(email, password, firstName, lastName, gender, membershipType)) =>
      if (!isDataValid(email, password)) {
        log.warning(s"$email or $password are not in a valid format")
        sender() ! InvalidData
      } else {
        val newUser = User(None, email, hashPassword(password), firstName, lastName,gender, membershipType)
        val userRegistrationResultFuture = for {
          existingUser <- userDB.findByUserEmail(email)
          userRegistrationResult <- handleUserRegistrationQueryResponse(existingUser, newUser)
        } yield userRegistrationResult

        userRegistrationResultFuture
          .mapTo[UserAuthResult]
          .pipeTo(sender())

      }

    case LoginUser(UserLoginRequest(email, password)) =>
      log.info(s"$email requesting to login")
      userDB.findByUserEmail(email)
        .map(handleLoginQueryResponse(_,email,password))
        .mapTo[UserAuthResult]
        .pipeTo(sender())

  }

  private def handleUserRegistrationQueryResponse(existingUser: Option[User], newUser: User): Future[UserAuthResult] = existingUser match {
    case Some(existingUser) =>
      log.info(s"User with email: ${existingUser.email} already exists")
      Future.successful(UserExists)
    case None =>
      userDB.insert(newUser).map(registeredUser => Successful(generateToken(registeredUser.id.get, registeredUser.membershipType)))
  }

  private def handleLoginQueryResponse(userOption: Option[User], email: String, password: String): UserAuthResult = userOption match {
    case Some(user) if checkPassword(password, user.password) =>
      log.info(s"User with email: ${user.email} exists")
      Successful(generateToken(user.id.get, user.membershipType))
    case Some(_) =>
      log.info(s"Incorrect password provided for User: $email")
      InvalidData
    case None =>
      log.info(s"User with email $email has not yet registered")
      UserNonExistent
  }



}
package com.jakway.util

import java.io.File

import com.jakway.gnucash.error.{ValidateF, ValidationError}

import scala.util.{Failure, Success, Try}

object Util {
  def anyOf[A](s: TraversableOnce[A])(f: A => Boolean): Boolean = s.foldLeft(false) {
    case (b, x) => b || f(x)
  }

  def reEither[A, B, C, D](a: Either[A, B])(fLeft: A => C)(fRight: B => D): Either[C, D] = a match {
    case Right(x) => Right(fRight(x))
    case Left(y) => Left(fLeft(y))
  }

  def either[A, B, C](a: Either[A, B])(ifLeft: A => C)(ifRight: B => C): C = a match {
    case Right(x) => ifRight(x)
    case Left(y) => ifLeft(y)
  }
}

object FileUtils {
  case class DeleteDirAndContentsException(val msg: String)
    extends RuntimeException(msg)

  /**
    * java equivalent of rm -r -f
    * @param d
    */
  def rmAll(d: File): Unit = {
    if(d.isDirectory()) {
      d.listFiles().foreach { f =>
        if(f.isDirectory) {
          rmAll(f)
        } else {
          if(!f.delete()) {
            throw DeleteDirAndContentsException(s"Could not delete $f" +
              s" in directory $d")
          }
        }
      }
      if(!d.delete()) {
        throw DeleteDirAndContentsException(s"Could not delete directory $d")
      }

    } else {
      if(!d.delete()) {
        throw DeleteDirAndContentsException(s"Could not delete file $d")
      }
    }
  }

}

/**
  * ValidateF version of the above
  */
object FileUtilsValidateF {
  val rmAll: ValidateF[File, Unit] =
    (d: File, errorType: String => ValidationError) => {
      Try(FileUtils.rmAll(d)) match {
        case Success(_) => Right(())
        case Failure(t) => Left(errorType(
          s"Could not delete directory $d").withCause(t))
      }
    }
}

package ldbc.snb.datagen

import java.io.Closeable

object syntax {
  implicit class FluentSyntaxForAny[A](val self: A) extends AnyVal {
    /**
     * Fluent syntax for folding with the target as the base item.
     */
    def withFoldLeft[F](foldable: TraversableOnce[F], op: (A, F) => A): A = {
      foldable.foldLeft(self)(op)
    }

    /**
     * Fluent syntax for applying a function on self.
     */
    def let[R](f: A => R): R = f(self)
  }

  // "try with resources"
  implicit class UseSyntaxForClosable[A <: Closeable](val self: A) extends AnyVal {
    def use[B](f: A => B): B = {
      val res = self
      try {
        f(res)
      } finally {
        self.close()
      }
    }
  }
}

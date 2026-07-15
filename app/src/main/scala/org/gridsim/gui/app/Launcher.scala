package org.gridsim.gui.app

/** Launcher class for the GridSim GUI application.
  *
  * This class does not extend {@link scalafx.application.JFXApp3} or
  * {@link javafx.application.Application}, which allows the application to be
  * launched from a fat/uber JAR on the classpath without triggering JVM-level
  * JavaFX module system checks.
  */
object Launcher:
  def main(args: Array[String]): Unit =
    GuiApp.main(args)

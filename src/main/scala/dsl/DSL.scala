package org.unibo.scooby
package dsl
import core.scooby.Configuration
import dsl.Export.ExportContext

import scala.compiletime.uninitialized

object DSL:

  export Config.*
  export Crawl.*
  export Scrape.*
  export Export.*


  class ConfigurationBuilder[T]():
    var configuration: Configuration[T] = uninitialized
    var exportContext: ExportContext[T] = uninitialized

    def build: Configuration[T] = configuration

    
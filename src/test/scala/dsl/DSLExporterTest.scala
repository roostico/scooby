package io.github.scooby
package dsl

import io.github.scooby.core.exporter.Exporter.Formats
import io.github.scooby.core.scraper.ScraperPolicies.ScraperPolicy
import io.github.scooby.utility.result.Result
import io.github.scooby.core.scraper.ScraperPolicies
import io.github.scooby.dsl.util.ScoobyTest
import io.github.scooby.utility.document.ScrapeDocument
import io.github.scooby.utility.document.html.HTMLElement
import io.github.scooby.utility.http.HttpError
import io.github.scooby.utility.http.api.Calls.GET

import scala.compiletime.uninitialized

class DSLExporterTest extends ScoobyTest:

  var expected: String = uninitialized
  var expectedIterable: Iterable[String] = uninitialized

  val scrapePolicy: ScraperPolicy[HTMLElement] = doc => doc.getAllElements
  val elemPolicy: HTMLElement => String = _.tag
  val resultPolicy: Iterable[HTMLElement] => Result[String] = it => Result(it.get(tag))

  "Exporter with batch write on file" should "correctly write on file final result" in:

    val docEither: Either[HttpError, ScrapeDocument] = GET(baseURL)
    val results = scrapePolicy(docEither.getOrElse(fail()))

    expected = Formats.string(resultPolicy(results))

    val filePath = path.resolve("exporter.txt")

    mockedScooby:
      exports:
        batch:
          strategy:
            results get tag output:
              toFile(filePath.toString) withFormat text
    .scrapeExportInspectFileContains(baseURL, filePath, expected, scrapePolicy)


  "Exporter with stream write on console" should "correctly output all result's items" in:

    val docEither: Either[HttpError, ScrapeDocument] = GET(baseURL)
    expectedIterable = scrapePolicy(docEither.getOrElse(fail())).map(elemPolicy)

    val separator = System.lineSeparator()

    mockedScooby:
      exports:
        streaming:
          results get tag output:
            toConsole withFormat text
    .scrapeExportInspectConsoleContains(baseURL, expectedIterable, separator, scrapePolicy)

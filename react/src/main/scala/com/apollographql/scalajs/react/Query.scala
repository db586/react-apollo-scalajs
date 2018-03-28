package com.apollographql.scalajs.react

import com.apollographql.scalajs.{GraphQLQuery, ParsedQuery}
import slinky.core.ExternalComponent
import slinky.core.facade.ReactElement
import slinky.readwrite.{Reader, Writer}

import scala.scalajs.js
import scala.scalajs.js.|

case class QueryData[T](loading: Boolean, error: Option[Error], data: Option[T], refetch: () => Unit)
object QueryData {
  implicit def reader[T](implicit tReader: Reader[T]): Reader[QueryData[T]] = { o =>
    val dyn = o.asInstanceOf[js.Dynamic]
    val loading = Reader.booleanReader.read(dyn.loading.asInstanceOf[js.Object])
    QueryData(
      loading,
      Reader.optionReader[Error].read(dyn.error.asInstanceOf[js.Object]),
      if (loading) None else Some(tReader.read(dyn.data.asInstanceOf[js.Object])),
      implicitly[Reader[() => Unit]].read(dyn.refetch.asInstanceOf[js.Object])
    )
  }
}

case class QueryOptions(pollInterval: js.UndefOr[Double] = js.undefined,
                        notifyOnNetworkStatusChange: js.UndefOr[Boolean] = js.undefined,
                        fetchPolicy: js.UndefOr[String] = js.undefined,
                        errorPolicy: js.UndefOr[String] = js.undefined,
                        ssr: js.UndefOr[Boolean] = js.undefined,
                        displayName: js.UndefOr[String] = js.undefined,
                        delay: js.UndefOr[Boolean] = js.undefined,
                        context: js.UndefOr[js.Object] = js.undefined)

object Query extends ExternalComponent {
  case class Props(query: ParsedQuery,
                   children: js.Object => ReactElement,
                   variables: js.UndefOr[js.Object] = js.undefined,
                   pollInterval: js.UndefOr[Double] = js.undefined,
                   notifyOnNetworkStatusChange: js.UndefOr[Boolean] = js.undefined,
                   fetchPolicy: js.UndefOr[String] = js.undefined,
                   errorPolicy: js.UndefOr[String] = js.undefined,
                   ssr: js.UndefOr[Boolean] = js.undefined,
                   displayName: js.UndefOr[String] = js.undefined,
                   delay: js.UndefOr[Boolean] = js.undefined,
                   context: js.UndefOr[js.Object] = js.undefined)
  def apply[T, V](query: ParsedQuery, variables: V, queryOptions: QueryOptions)
                 (children: QueryData[T] => ReactElement)
                 (implicit tReader: Reader[T], vWriter: Writer[V]): slinky.core.BuildingComponent[Element, js.Object] = {
    val queryDataReader = QueryData.reader(tReader)
    apply(Props(
      query = query,
      variables = vWriter.write(variables),
      children = d => {
        children(queryDataReader.read(d))
      },
      // queryOptions
      pollInterval = queryOptions.pollInterval,
      notifyOnNetworkStatusChange = queryOptions.notifyOnNetworkStatusChange,
      fetchPolicy = queryOptions.fetchPolicy,
      errorPolicy = queryOptions.errorPolicy,
      ssr = queryOptions.ssr,
      displayName = queryOptions.displayName,
      delay = queryOptions.delay,
      context = queryOptions.context
    ))
  }

  def apply[T, V](query: ParsedQuery, variables: V)
                 (children: QueryData[T] => ReactElement)
                 (implicit tReader: Reader[T], vWriter: Writer[V]): slinky.core.BuildingComponent[Element, js.Object] = {
    apply[T, V](
      query = query,
      variables = variables,
      queryOptions = QueryOptions()
    )(children)
  }

  def apply[T](query: ParsedQuery, queryOptions: QueryOptions)
              (children: QueryData[T] => ReactElement)
              (implicit tReader: Reader[T]): slinky.core.BuildingComponent[Element, js.Object] = {
    apply[T, Unit](
      query = query,
      (),
      queryOptions
    )(children)
  }

  def apply[T](query: ParsedQuery)
              (children: QueryData[T] => ReactElement)
              (implicit tReader: Reader[T]): slinky.core.BuildingComponent[Element, js.Object] = {
    apply[T](
      query = query,
      queryOptions = QueryOptions()
    )(children)
  }

  def apply[Q <: GraphQLQuery](query: Q, variables: Q#Variables, queryOptions: QueryOptions)
                              (children: QueryData[query.Data] => ReactElement)
                              (implicit dataReader: Reader[query.Data],
                               variablesWriter: Writer[Q#Variables]): slinky.core.BuildingComponent[Element, js.Object] = {
    apply[query.Data, Q#Variables](
      query = query.operation,
      variables = variables,
      queryOptions = queryOptions
    )(children)
  }

  def apply[Q <: GraphQLQuery](query: Q, variables: Q#Variables)
                              (children: QueryData[query.Data] => ReactElement)
                              (implicit dataReader: Reader[query.Data],
                               variablesWriter: Writer[Q#Variables]): slinky.core.BuildingComponent[Element, js.Object] = {
    apply[Q](
      query = query,
      variables = variables,
      queryOptions = QueryOptions()
    )(children)
  }

  def apply(query: GraphQLQuery { type Variables = Unit }, queryOptions: QueryOptions)
           (children: QueryData[query.Data] => ReactElement)
           (implicit dataReader: Reader[query.Data]): slinky.core.BuildingComponent[Element, js.Object] = {
    apply[GraphQLQuery {type Variables = Unit }](
      query = query,
      variables = (),
      queryOptions = queryOptions
    )(children)
  }

  def apply(query: GraphQLQuery { type Variables = Unit })
           (children: QueryData[query.Data] => ReactElement)
           (implicit dataReader: Reader[query.Data]): slinky.core.BuildingComponent[Element, js.Object] = {
    apply(
      query = query,
      queryOptions = QueryOptions()
    )(children)
  }

  override val component: |[String, js.Object] = ReactApollo.Query
}

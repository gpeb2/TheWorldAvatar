export interface DocumentCollection {
  type: "document_collection"
  data: { [key: string]: any }[]
}

export type TableDataValue = string | number | string[] | number[] | TableDataBase | null

export interface TableDataRow {
  [key: string]: TableDataValue
}

export interface TableDataBase {
  columns: string[]
  data: TableDataRow[]
}

export interface TableData extends TableDataBase {
  type: "table"
}

export interface TypedSeries {
  data: any[]
  type: string
}

export interface ScatterPlotTrace {
  name?: string
  x: TypedSeries
  y: TypedSeries
}

export interface ScatterPlotData {
  type: "scatter_plot"
  title: string
  traces: ScatterPlotTrace[]
}

export interface WKTGeometryData {
  type: "wkt_geometry"
  srs: "crs84"
  title?: string
  literal: string
}

export type DataItem = DocumentCollection | TableData | ScatterPlotData | WKTGeometryData 

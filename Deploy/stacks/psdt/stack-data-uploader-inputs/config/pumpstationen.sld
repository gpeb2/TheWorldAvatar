<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd"
  xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

  <NamedLayer>
    <Name>SewageIconStyle</Name>
    <UserStyle>
      <Title>sewage icon point style</Title>
      <FeatureTypeStyle>
        <Rule>
          <Title>icon point</Title>
          <PointSymbolizer>
            <Graphic>
              <ExternalGraphic>
                <OnlineResource xlink:type="simple" xlink:href="http://psdt-geoserver:8080/geoserver/www/icons/aw-sewage.png" />
                <Format>image/png</Format>
              </ExternalGraphic>
            </Graphic>
            <VendorOption name="labelObstacle">true</VendorOption>
          </PointSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>

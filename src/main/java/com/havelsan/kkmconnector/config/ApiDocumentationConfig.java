package com.havelsan.kkmconnector.config;

import io.swagger.annotations.*;

@SwaggerDefinition(
        info = @Info(
                description = "This document created for KKM-Connector usage info",
                version = "V1.0.0",
                title = "KKM-TGKM-Connector Resource API",
                contact = @Contact(
                        name = "Erkan Onat",
                        email = "eonat@havelsan.com.tr",
                        url = "http://10.151.102.58:8080"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        consumes = {"application/json", "application/xml"},
        produces = {"application/json", "application/xml"},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        externalDocs = @ExternalDocs(value = "Read This For Sure", url = "http://10.151.102.58:8080")
)
public class ApiDocumentationConfig {
}

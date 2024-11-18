-- Create table eg_pgr_service_v2
CREATE TABLE eg_pgr_service_v2 (
    id                  CHARACTER VARYING(64),
    tenantId            CHARACTER VARYING(256),
    serviceCode         CHARACTER VARYING(256) NOT NULL,
    serviceRequestId    CHARACTER VARYING(256),
    description         CHARACTER VARYING(4000),
    accountId           CHARACTER VARYING(256),
    additionalDetails   JSONB,
    applicationStatus   CHARACTER VARYING(128),
    rating              SMALLINT,
    source              CHARACTER VARYING(256),
    createdby           CHARACTER VARYING(256) NOT NULL,
    createdtime         BIGINT NOT NULL,
    lastmodifiedby      CHARACTER VARYING(256),
    lastmodifiedtime    BIGINT,
    active              BOOLEAN DEFAULT TRUE,
    CONSTRAINT uk_eg_pgr_service_v2 UNIQUE (id),
    CONSTRAINT pk_eg_pgr_serviceReq_v2 PRIMARY KEY (tenantId, serviceRequestId)
);

-- Create table eg_pgr_address_v2
CREATE TABLE eg_pgr_address_v2 (
    tenantId          CHARACTER VARYING(256) NOT NULL,
    id                CHARACTER VARYING(256) NOT NULL,
    parentid          CHARACTER VARYING(256) NOT NULL,
    doorno            CHARACTER VARYING(128),
    plotno            CHARACTER VARYING(256),
    buildingName      CHARACTER VARYING(1024),
    street            CHARACTER VARYING(1024),
    landmark          CHARACTER VARYING(1024),
    city              CHARACTER VARYING(512),
    pincode           CHARACTER VARYING(16),
    locality          CHARACTER VARYING(128) NOT NULL,
    district          CHARACTER VARYING(256),
    region            CHARACTER VARYING(256),
    state             CHARACTER VARYING(256),
    country           CHARACTER VARYING(512),
    latitude          NUMERIC(9,6),
    longitude         NUMERIC(10,7),
    createdby         CHARACTER VARYING(128) NOT NULL,
    createdtime       BIGINT NOT NULL,
    lastmodifiedby    CHARACTER VARYING(128),
    lastmodifiedtime  BIGINT,
    additionaldetails JSONB,
    CONSTRAINT pk_eg_pgr_address_v2 PRIMARY KEY (id),
    CONSTRAINT fk_eg_pgr_address_v2 FOREIGN KEY (parentid) REFERENCES eg_pgr_service_v2 (id)
);

-- Create indexes for eg_pgr_service_v2
CREATE INDEX IF NOT EXISTS index_eg_pgr_service_v2_tenantId_serviceRequestId ON eg_pgr_service_v2 (tenantId, serviceRequestId);
CREATE INDEX IF NOT EXISTS index_eg_pgr_service_v2_id ON eg_pgr_service_v2 (id);
CREATE INDEX IF NOT EXISTS index_eg_pgr_service_v2_applicationStatus ON eg_pgr_service_v2 (applicationStatus);
CREATE INDEX IF NOT EXISTS index_eg_pgr_service_v2_serviceCode ON eg_pgr_service_v2 (serviceCode);
CREATE INDEX IF NOT EXISTS index_eg_pgr_service_v2_accountId ON eg_pgr_service_v2 (accountId);

-- Create indexes for eg_pgr_address_v2
CREATE INDEX IF NOT EXISTS index_eg_pgr_address_v2_locality ON eg_pgr_address_v2 (locality);

-- Modify eg_pgr_service_v2
ALTER TABLE eg_pgr_service_v2 ALTER COLUMN description DROP NOT NULL;

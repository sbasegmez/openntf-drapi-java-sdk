package org.openntf.drapi.meta;

/**
 * This record is a Scope data schema. Don't confuse it with the DrapiDataSource interface, which is a representation of an API context
 * on DRAPI and incidentally referred to as Scope in the DRAPI interface. This record is used to define the structure of a scope,
 * including its name, NSF path, schema name, icon name, icon, description, and active status.
 */
public record Scope(String apiName, String nsfPath, String schemaName, String iconName, String icon, String description,
                    boolean isActive) {

}

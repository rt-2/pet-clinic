package dev.leswilson.petclinic.listeners;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class NamingStrategy implements PhysicalNamingStrategy {
    private static final Map<String,String> ABBREVIATIONS = buildAbbreviationMap();

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        // Acme naming standards do not apply to catalog names
        log.info("toPhysicalCatalogName:"+name);
        return name;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        log.info("toPhysicalSchemaName:"+name);
        // Acme naming standards do not apply to schema names
        return null;
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        log.info("toPhysicalTableName:"+name);
        final List<String> parts = splitAndReplace( name.getText() );
        return jdbcEnvironment.getIdentifierHelper().toIdentifier(
                join( parts ),
                name.isQuoted()
        );
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        log.info("toPhysicalSequenceName:"+name);
        final LinkedList<String> parts = splitAndReplace( name.getText() );
        // Acme Corp says all sequences should end with _seq
        if ( !"seq".equalsIgnoreCase( parts.getLast() ) ) {
            parts.add( "seq" );
        }
        return jdbcEnvironment.getIdentifierHelper().toIdentifier(
                join( parts ),
                name.isQuoted()
        );
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        log.info("toPhysicalColumnName:"+name);
        final List<String> parts = splitAndReplace( name.getText() );
        return jdbcEnvironment.getIdentifierHelper().toIdentifier(
                join( parts ),
                name.isQuoted()
        );
    }

    private static Map<String, String> buildAbbreviationMap() {
        TreeMap<String,String> abbreviationMap = new TreeMap<> ( String.CASE_INSENSITIVE_ORDER );
        abbreviationMap.put( "account", "acct" );
        abbreviationMap.put( "number", "num" );
        return abbreviationMap;
    }

    private LinkedList<String> splitAndReplace(String name) {
        LinkedList<String> result = new LinkedList<>();
        for ( String part : StringUtils.splitByCharacterTypeCamelCase( name ) ) {
            if ( part == null || part.trim().isEmpty() || "_".equals(part)) {
                // skip null and space
                continue;
            }
            part = applyAbbreviationReplacement( part );
            result.add(part.toLowerCase(Locale.ROOT));
        }
        return result;
    }

    private String applyAbbreviationReplacement(String word) {
        if ( ABBREVIATIONS.containsKey( word ) ) {
            return ABBREVIATIONS.get( word );
        }

        return word;
    }

    private String join(List<String> parts) {
        boolean firstPass = true;
        String separator = "";
        StringBuilder joined = new StringBuilder();
        for ( String part : parts ) {
            joined.append( separator ).append( part );
            if ( firstPass ) {
                firstPass = false;
                separator = "_";
            }
        }
        return joined.toString();
    }
}

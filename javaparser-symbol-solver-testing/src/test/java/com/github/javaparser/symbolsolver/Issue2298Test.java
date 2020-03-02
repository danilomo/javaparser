package com.github.javaparser.symbolsolver;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseStart;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.StreamProvider;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Issue2298Test extends AbstractSymbolResolutionTest {

    @Test
    public void test() throws IOException {
        Path file = adaptPath("src/test/resources/issue2298/Test.java");

        CombinedTypeSolver combinedSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        ConfigurableSymbolSolver css = new ConfigurableSymbolSolver(new JavaSymbolSolver(combinedSolver));
        
        css.setStrategy(MethodCallExpr.class, new FindFirstMethodSameName());
        
        ParserConfiguration pc = new ParserConfiguration()
                .setSymbolResolver(css)
                .setLanguageLevel(LanguageLevel.JAVA_8);

        JavaParser javaParser = new JavaParser(pc);

        CompilationUnit unit = javaParser.parse(ParseStart.COMPILATION_UNIT,
                new StreamProvider(Files.newInputStream(file))).getResult().get();

        MethodCallExpr mce = unit.findFirst(MethodCallExpr.class).get();
        
        Assertions.assertEquals(mce.calculateResolvedType(), ResolvedPrimitiveType.INT);

    }
    
    private static class FindFirstMethodSameName implements SolvingStrategy<MethodCallExpr>{        
        @Override
        public Optional<ResolvedType> resolveType(MethodCallExpr expr) {
            Optional<ResolvedType> scope = expr
                    .getScope()
                    .map( e -> e.calculateResolvedType() )
                    .filter( e -> e != ResolvedType.NOT_RESOLVED );
            
            Optional<ResolvedReferenceType> ref = scope
                    .filter( t -> t.isReferenceType() )
                    .map( t -> t.asReferenceType() );
            
            return ref.flatMap( r -> r.getAllMethods()
                    .stream()
                    .filter( m -> m.getName().equals(expr.getNameAsString()))
                    .findAny())
                    .map( rmd -> rmd.getReturnType() );
        }
    }
    
    
}

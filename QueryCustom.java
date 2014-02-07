import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.com.tbl.nfse.domain.model.RetornoConsultaBanco;

/**
 * CLASSE AUXILIAR QUE UTILIZA OO JPQL PARA REALIZAR CONSULTAS COM POSSIBILIDADE
 * DE CONTROLAR PAGINAÇÃO DE REGISTROS O FUNCIONAMENTO DESTA CLASSE DEVE: 1-
 * USAR SEU CONSTRUTOR PARA POPULAR OS DADOS NECESSÁRIOS PARA O FUNCIONAMENTO DA
 * CONSULTA. 2- USAR O MÉTODO addCriteria N VEZES PARA POPULAR OS FILTROS QUE A
 * CONSULTA PODERÁ TER. 3- USAR O MÉTODO createQueriesJpa PARA CRIAR AS QUERIES
 * JPA. 4- USAR O MÉTODO setParameters N VEZES PARA POPULAR OS FILTROS DA
 * CONSULTA 5- USAR O MÉTODO queryByRange PARA RECEBER UM OBJETO COM O RETORNO
 * DA CONSULTA DOS DADOS.
 */
public class QueryCustom<T> {

	private final String SQL_NORMAL = " SELECT o FROM ";
	private final String SQL_COUNT = " SELECT COUNT(o) FROM ";
	List<String> criteriaString = new ArrayList<String>();
	StringBuffer queryString = new StringBuffer();
	private Query queryNormal;
	private Query queryCount;
	private int firstResult;
	private int maxResults;

	/**
	 * CONSTRUTOR QUE DADOS NECESSÁRIOS PARA O FUNCIONAMENTO DA CONSULTA
	 * 
	 * @param sqlInit
	 *            (DEPOIS DO FROM )
	 * @param alias
	 *            (ALIAS INFORMADO JUNTO A SQLiNIT)
	 * @param firstResult
	 *            (PRIMEIRO REGISTRO A SER RECUPERADO)
	 * @param maxResults
	 *            (ÚLTIMO REGISTRO A SER RECUPERADO, INFORMAR 0 QUANDO NÃO
	 *            PRECISAR DE PAGINAÇÃO)
	 */
	public QueryCustom(String sqlInit, boolean alias, int firstResult, int maxResults) {
		try {
			this.queryString.append(sqlInit + (alias ? "" : " o "));
			this.firstResult = firstResult;
			this.maxResults = maxResults;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * MÉTODO QUE VERIFICA SE HAVERÁ CONTROLE DE PAGINAÇÃO
	 * 
	 * @return
	 */
	private boolean isPagination() {
		return maxResults == 0 ? false : true;
	}

	/**
	 * MÉTODO QUE ADICIONA FILTROS A CONSULTA A SER CRIADA
	 * 
	 * @param criteria
	 */
	public void addCriteria(String criteria) {
		criteriaString.add(criteria);
	}

	/**
	 * METODO QUE REALIZA A CONVERSÃO DA CONSULTA INFORMADA PARA QUERIES DO JPA
	 * COM POSSIVEL CONTROLE DE PAGINAÇÃO
	 */
	public void createQueriesJpa(EntityManager entityManager, String orderBy) {
		try {
			populateQueryWithCriterias();
			orderBy = orderBy == null ? "" : orderBy;
			queryNormal = entityManager.createQuery(SQL_NORMAL + queryString.toString() + orderBy);
			if (isPagination()) {
				queryCount = entityManager.createQuery(SQL_COUNT + queryString.toString() + orderBy);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * MÉTODO INTERNO QUE POPULA A CONSULTA COM OS FILTROS INFORMADOS
	 * ADICIONANDO AS CLÁUSULAS NECESSÁRIAS PARA REALIZAR A CONSULTA
	 */
	private void populateQueryWithCriterias() {
		try {
			for (int i = 0; i < criteriaString.size(); i++) {
				queryString.append((i > 0) ? " AND " + criteriaString.get(i) : " WHERE " + criteriaString.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * MÉTODO QU POPULA OS VALORES A SEREM USADOS NOS FILTROS INFORMADOS
	 * 
	 * @param key
	 * @param value
	 */
	public void setParameters(String key, Object value) {
		try {
			queryNormal.setParameter(key, value);
			if (isPagination()) {
				queryCount.setParameter(key, value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * MÉTODO QUE REALIZA A CONSULTA COM OU SEM PAGINAÇÃO E RETORNA O CONTEÚDO
	 * EM UM RETORNO CONSULTABANCO
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public RetornoConsultaBanco<T> queryByRange() throws Exception {
		RetornoConsultaBanco<T> consulta = new RetornoConsultaBanco<T>();
		try {
			consulta.setTotalRegistros(queryCount.getResultList().size());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		if (firstResult > 0) {
			queryNormal = queryNormal.setFirstResult(firstResult);
		}
		if (maxResults > 0) {
			queryNormal = queryNormal.setMaxResults(maxResults);
		}
		try {
			consulta.setLista(queryNormal.getResultList());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return consulta;
	}

	public Query getQueryNormal() {
		return queryNormal;
	}

	public Query getQueryCount() {
		return queryCount;
	}

	public int getFirstResult() {
		return firstResult;
	}

	public int getMaxResults() {
		return maxResults;
	}

}

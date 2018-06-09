package ar.com.rodrilapenta.favsites.db.repository;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import ar.com.rodrilapenta.favsites.db.DatabaseHelper;
import ar.com.rodrilapenta.favsites.db.model.WebInstance;

/**
 * Created by arielverdugo on 6/6/17.
 */

public class WebInstanceRepository
{
    private static WebInstanceRepository instance;

    private Dao<WebInstance, Integer> dao;

    public static WebInstanceRepository getInstance(Context context) {
        if(instance == null)
            instance = new WebInstanceRepository(context);
        return instance;
    }

    private WebInstanceRepository(Context context){
        OrmLiteSqliteOpenHelper helper =  OpenHelperManager.getHelper(context, DatabaseHelper.class);
        try {
            dao = helper.getDao(WebInstance.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<WebInstance> getWebInstances(){
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addWebInstance(WebInstance wi){
        try {
            dao.create(wi);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*public WebInstance findUsuarioById(int id){
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteUsuarioById(int id){
        try {
            dao.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public List<Usuario> findWhere(String fieldName, Object value){
        try {
            return dao.queryForEq(fieldName, value);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //buscar n usuarios por n condiciones
    public List<Usuario> findWhere(Map<String, String> params){
        try {
            QueryBuilder<Usuario, Integer> queryBuilder = dao.queryBuilder();
            Where<Usuario, Integer> where = queryBuilder.where();

            for(Map.Entry<String, String> param: params.entrySet()) {
                where.eq(param.getKey(), param.getValue());
            }

            where.and(params.size());
            PreparedQuery<Usuario> preparedQuery = queryBuilder.prepare();
            return dao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuario findUniqueWhere(Map<String, String> params) {
        try {
            QueryBuilder<Usuario, Integer> queryBuilder = dao.queryBuilder();
            Where<Usuario, Integer> where = queryBuilder.where();

            for(Map.Entry<String, String> param: params.entrySet()) {
                where.eq(param.getKey(), param.getValue());
            }

            where.and(params.size());
            PreparedQuery<Usuario> preparedQuery = queryBuilder.prepare();
            List<Usuario> usuarios = dao.query(preparedQuery);
            if(usuarios.size() == 0) {
                return null;
            }
            else if(usuarios.size() > 1) {
                throw new Exception("HAY MAS DE UN USUARIO CON ESOS PARAMETROS");
            }
            else {
                return usuarios.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/
}

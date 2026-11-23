# SupaBaseExpo

Inventario físico con Expo, React Native y Supabase.

Permite registrar, consultar, buscar, editar y eliminar productos.

## Configuración

1. Instala las dependencias con `npm install`.
2. Copia `.env.example` como `.env` y define:
   - `EXPO_PUBLIC_SUPABASE_URL`
   - `EXPO_PUBLIC_SUPABASE_PUBLISHABLE_KEY`
3. Inicia el proyecto con `npx expo start`.

Las variables con prefijo `EXPO_PUBLIC_` se incluyen en el bundle de la aplicación. La clave publicable de Supabase está diseñada para uso en cliente; **nunca** pongas una clave `secret` o `service_role` aquí. Tras cambiar `.env`, reinicia Expo.

## Tabla requerida

La app consulta la tabla `productos` y espera las columnas `id`, `producto`, `descripcion` y `precio`. Configura las políticas de Row Level Security de Supabase para el rol que utilizará la app y limita las operaciones y filas según los usuarios autorizados. No desactives RLS ni expongas una clave privilegiada en el cliente.

## Comandos

- `npm start`: inicia Expo.
- `npm run android`: inicia Expo para Android.
- `npm run ios`: inicia Expo para iOS.
- `npm run web`: inicia Expo para web.
- `npm run lint`: revisa el código con ESLint.

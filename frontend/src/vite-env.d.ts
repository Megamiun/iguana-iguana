/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_MAPLEWOOD_BASE_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

// UI-specific type extensions
export type WithUIState<T> = T & {
    _paying?: boolean;
};
import { ReadonlyURLSearchParams } from 'next/navigation'
import { getJson, BACKEND_ENDPOINT, getReq } from '.'
import { ChemicalClass, Species, Use } from '../model/ontospecies'

const GET_CHEMICAL_CLASSES_ENDPOINT = new URL(
  './ontospecies/chemical-classes',
  BACKEND_ENDPOINT
)
export function getChemicalClasses() {
  return getJson<ChemicalClass[]>(GET_CHEMICAL_CLASSES_ENDPOINT, {
    next: { revalidate: 24 * 60 * 60 }, // revalidate every 24 hours
  })
}

const GET_USES_ENDPOINT = new URL('./ontospecies/uses', BACKEND_ENDPOINT)
export function getUses() {
  return getJson<Use[]>(GET_USES_ENDPOINT, {
    next: { revalidate: 24 * 60 * 60 }, // revalidate every 24 hours
  })
}

const GET_SPECIES_ENDPOINT = new URL('./ontospecies/species', BACKEND_ENDPOINT)
export function getSpeciesMany(searchParams: URLSearchParams) {
  return getJson<Species[]>(`${GET_SPECIES_ENDPOINT}?${searchParams}`, {
    next: { revalidate: 24 * 60 * 60 }, // revalidate every 24 hours
  })
}

export function getSpeciesOne(iriEncoded: string) {
  return getJson<Species>(`${GET_SPECIES_ENDPOINT}/${iriEncoded}`, {
    next: { revalidate: 24 * 60 * 60 }, // revalidate every 24 hours
  })
}

export function getSpeciesXYZ(iriEncoded: string) {
  return getReq(`${GET_SPECIES_ENDPOINT}/${iriEncoded}/xyz`).then(res =>
    res.text()
  )
}

const GET_SPECIES_PARTIAL_ENDPOINT = new URL(
  './ontospecies/species-partial',
  BACKEND_ENDPOINT
)
export function getSpeciesPartialMany(searchParams: URLSearchParams) {
  return getJson<Partial<Species>[]>(
    `${GET_SPECIES_PARTIAL_ENDPOINT}?${searchParams}`,
    {
      next: { revalidate: 24 * 60 * 60 }, // revalidate every 24 hours
    }
  )
}

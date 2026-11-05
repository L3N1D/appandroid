package ru.urfu.coinpulse.data

import java.util.Locale
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Deterministic mock data shaped after the public CoinGecko endpoints:
 *
 *  - `GET /coins/markets?vs_currency=usd&order=market_cap_desc&per_page=...&page=...`
 *  - `GET /coins/{id}?localization=false&tickers=false&market_data=false`
 *
 * The repository is the only place that knows the data is fake. Swapping in a real
 * network source later means implementing [CoinRepository] and changing one line in
 * [ru.urfu.coinpulse.CoinPulseApplication].
 */
object MockCoinData {

    internal data class CoinSeed(
        val id: String,
        val symbol: String,
        val name: String,
        val imageUrl: String,
        val price: Double,
        val change24h: Double,
        val change7d: Double,
        val marketCap: Long,
        val volume24h: Long,
        val circulatingSupply: Double,
        val maxSupply: Double?,
        val ath: Double,
        val atl: Double,
        val genesisDate: String,
        val algorithm: String?,
        val categories: List<String>,
        val tagline: String,
        val extra: String,
        val homepage: String,
        val githubUrl: String?,
        val subredditUrl: String?,
    )

    private val seeds: List<CoinSeed> = listOf(
        CoinSeed(
            id = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            imageUrl = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
            price = 97_432.18,
            change24h = 1.84,
            change7d = 4.32,
            marketCap = 1_928_000_000_000,
            volume24h = 42_300_000_000,
            circulatingSupply = 19_790_000.0,
            maxSupply = 21_000_000.0,
            ath = 108_268.0,
            atl = 67.81,
            genesisDate = "2009-01-03",
            algorithm = "SHA-256",
            categories = listOf("Layer 1", "Store of value", "Proof of Work"),
            tagline = "the original peer-to-peer electronic cash system",
            extra = "Bitcoin introduced the first practical implementation of a distributed ledger. " +
                "Its fixed issuance schedule and decentralised mining network made it the benchmark " +
                "asset of the crypto market.",
            homepage = "https://bitcoin.org",
            githubUrl = "https://github.com/bitcoin/bitcoin",
            subredditUrl = "https://www.reddit.com/r/Bitcoin/",
        ),
        CoinSeed(
            id = "ethereum",
            symbol = "ETH",
            name = "Ethereum",
            imageUrl = "https://assets.coingecko.com/coins/images/279/large/ethereum.png",
            price = 3_421.55,
            change24h = 2.61,
            change7d = 6.18,
            marketCap = 412_000_000_000,
            volume24h = 18_900_000_000,
            circulatingSupply = 120_400_000.0,
            maxSupply = null,
            ath = 4_878.26,
            atl = 0.43,
            genesisDate = "2015-07-30",
            algorithm = "Proof of Stake",
            categories = listOf("Layer 1", "Smart contract platform", "Proof of Stake"),
            tagline = "a programmable blockchain for decentralised applications",
            extra = "Ethereum popularised smart contracts and the EVM, which now underpins thousands " +
                "of tokens, rollups and DeFi protocols. The Merge moved the network to proof of stake " +
                "and cut its energy usage by more than 99%.",
            homepage = "https://ethereum.org",
            githubUrl = "https://github.com/ethereum/go-ethereum",
            subredditUrl = "https://www.reddit.com/r/ethereum/",
        ),
        CoinSeed(
            id = "tether",
            symbol = "USDT",
            name = "Tether",
            imageUrl = "https://assets.coingecko.com/coins/images/325/large/Tether.png",
            price = 1.00,
            change24h = 0.02,
            change7d = -0.01,
            marketCap = 143_500_000_000,
            volume24h = 62_000_000_000,
            circulatingSupply = 143_400_000_000.0,
            maxSupply = null,
            ath = 1.32,
            atl = 0.57,
            genesisDate = "2014-10-06",
            algorithm = null,
            categories = listOf("Stablecoin", "Fiat-backed"),
            tagline = "a dollar-pegged stablecoin used to move liquidity between venues",
            extra = "Each USDT token is intended to be backed by cash and cash-equivalent reserves. " +
                "It is the most traded pair on most centralised exchanges and a common quote currency " +
                "for altcoins.",
            homepage = "https://tether.to",
            githubUrl = null,
            subredditUrl = "https://www.reddit.com/r/Tether/",
        ),
        CoinSeed(
            id = "xrp",
            symbol = "XRP",
            name = "XRP",
            imageUrl = "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png",
            price = 2.38,
            change24h = -0.87,
            change7d = 2.44,
            marketCap = 136_000_000_000,
            volume24h = 4_800_000_000,
            circulatingSupply = 57_100_000_000.0,
            maxSupply = 100_000_000_000.0,
            ath = 3.84,
            atl = 0.00268,
            genesisDate = "2013-01-01",
            algorithm = "Consensus protocol",
            categories = listOf("Payment", "Layer 1"),
            tagline = "a fast settlement asset for cross-border payments",
            extra = "The XRP Ledger confirms transactions in a few seconds using a federated consensus " +
                "mechanism rather than mining. Financial institutions have used it as a bridge asset " +
                "for liquidity routing.",
            homepage = "https://xrpl.org",
            githubUrl = "https://github.com/XRPLF/rippled",
            subredditUrl = "https://www.reddit.com/r/Ripple/",
        ),
        CoinSeed(
            id = "binancecoin",
            symbol = "BNB",
            name = "BNB",
            imageUrl = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png",
            price = 712.40,
            change24h = 1.12,
            change7d = 3.07,
            marketCap = 102_800_000_000,
            volume24h = 2_100_000_000,
            circulatingSupply = 144_300_000.0,
            maxSupply = 200_000_000.0,
            ath = 793.35,
            atl = 0.10,
            genesisDate = "2017-07-25",
            algorithm = "Proof of Staked Authority",
            categories = listOf("Exchange token", "Layer 1", "Smart contract platform"),
            tagline = "the native asset of the BNB Chain ecosystem",
            extra = "BNB powers transaction fees, staking and governance on BNB Chain and BNB Smart Chain. " +
                "Quarterly burns have reduced its supply from the original 200 million tokens.",
            homepage = "https://www.bnbchain.org",
            githubUrl = "https://github.com/bnb-chain",
            subredditUrl = "https://www.reddit.com/r/BNBChain/",
        ),
        CoinSeed(
            id = "solana",
            symbol = "SOL",
            name = "Solana",
            imageUrl = "https://assets.coingecko.com/coins/images/4128/large/solana.png",
            price = 198.62,
            change24h = 4.73,
            change7d = 11.86,
            marketCap = 94_600_000_000,
            volume24h = 5_400_000_000,
            circulatingSupply = 476_000_000.0,
            maxSupply = null,
            ath = 294.33,
            atl = 0.50,
            genesisDate = "2020-03-16",
            algorithm = "Proof of History",
            categories = listOf("Layer 1", "Smart contract platform", "High performance"),
            tagline = "a high-throughput blockchain focused on low fees and fast finality",
            extra = "Proof of History timestamps transactions before consensus, allowing the network to " +
                "process thousands of transactions per second. Solana hosts a large share of consumer " +
                "crypto apps, payments and NFT activity.",
            homepage = "https://solana.com",
            githubUrl = "https://github.com/solana-labs/solana",
            subredditUrl = "https://www.reddit.com/r/solana/",
        ),
        CoinSeed(
            id = "dogecoin",
            symbol = "DOGE",
            name = "Dogecoin",
            imageUrl = "https://assets.coingecko.com/coins/images/5/large/dogecoin.png",
            price = 0.3842,
            change24h = -1.56,
            change7d = 5.72,
            marketCap = 56_300_000_000,
            volume24h = 9_800_000_000,
            circulatingSupply = 146_600_000_000.0,
            maxSupply = null,
            ath = 0.7315,
            atl = 0.00008,
            genesisDate = "2013-12-06",
            algorithm = "Scrypt",
            categories = listOf("Meme", "Payment"),
            tagline = "a community-driven coin that started as a joke",
            extra = "Dogecoin forked from Litecoin and kept a fixed yearly issuance of 5 billion coins. " +
                "Low fees and a very active community made it one of the most recognisable crypto brands.",
            homepage = "https://dogecoin.com",
            githubUrl = "https://github.com/dogecoin/dogecoin",
            subredditUrl = "https://www.reddit.com/r/dogecoin/",
        ),
        CoinSeed(
            id = "usd-coin",
            symbol = "USDC",
            name = "USDC",
            imageUrl = "https://assets.coingecko.com/coins/images/6319/large/usdc.png",
            price = 0.9998,
            change24h = -0.01,
            change7d = 0.03,
            marketCap = 41_200_000_000,
            volume24h = 8_300_000_000,
            circulatingSupply = 41_200_000_000.0,
            maxSupply = null,
            ath = 1.17,
            atl = 0.88,
            genesisDate = "2018-09-26",
            algorithm = null,
            categories = listOf("Stablecoin", "Fiat-backed"),
            tagline = "a fully reserved dollar stablecoin issued by Circle",
            extra = "USDC is backed by short-dated US Treasuries and cash held in regulated custody. " +
                "Monthly attestations and broad exchange support make it a standard settlement asset " +
                "for on-chain finance.",
            homepage = "https://www.circle.com/usdc",
            githubUrl = "https://github.com/circlefin",
            subredditUrl = "https://www.reddit.com/r/USDC/",
        ),
        CoinSeed(
            id = "cardano",
            symbol = "ADA",
            name = "Cardano",
            imageUrl = "https://assets.coingecko.com/coins/images/975/large/cardano.png",
            price = 1.09,
            change24h = 0.94,
            change7d = -2.15,
            marketCap = 38_400_000_000,
            volume24h = 1_600_000_000,
            circulatingSupply = 35_200_000_000.0,
            maxSupply = 45_000_000_000.0,
            ath = 3.09,
            atl = 0.01925,
            genesisDate = "2017-09-29",
            algorithm = "Ouroboros",
            categories = listOf("Layer 1", "Smart contract platform", "Proof of Stake"),
            tagline = "a research-driven proof-of-stake smart contract platform",
            extra = "Cardano is developed around peer-reviewed protocols and a formal specification. " +
                "Its extended UTXO model supports native assets, while governance is progressively " +
                "handed to ADA holders.",
            homepage = "https://cardano.org",
            githubUrl = "https://github.com/input-output-hk/cardano-node",
            subredditUrl = "https://www.reddit.com/r/cardano/",
        ),
        CoinSeed(
            id = "tron",
            symbol = "TRX",
            name = "TRON",
            imageUrl = "https://assets.coingecko.com/coins/images/1094/large/tron-logo.png",
            price = 0.2675,
            change24h = 2.08,
            change7d = 4.91,
            marketCap = 23_100_000_000,
            volume24h = 1_200_000_000,
            circulatingSupply = 86_400_000_000.0,
            maxSupply = null,
            ath = 0.44,
            atl = 0.00180,
            genesisDate = "2017-09-13",
            algorithm = "Delegated Proof of Stake",
            categories = listOf("Layer 1", "Smart contract platform", "Payment"),
            tagline = "a high-throughput network popular for stablecoin transfers",
            extra = "TRON uses a delegated proof-of-stake model with 27 elected block producers. " +
                "It carries a large share of global USDT transfers thanks to low fees and fast blocks.",
            homepage = "https://tron.network",
            githubUrl = "https://github.com/tronprotocol",
            subredditUrl = "https://www.reddit.com/r/Tronix/",
        ),
        CoinSeed(
            id = "avalanche-2",
            symbol = "AVAX",
            name = "Avalanche",
            imageUrl = "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png",
            price = 42.18,
            change24h = -2.32,
            change7d = 1.64,
            marketCap = 17_200_000_000,
            volume24h = 720_000_000,
            circulatingSupply = 408_000_000.0,
            maxSupply = 720_000_000.0,
            ath = 144.96,
            atl = 2.80,
            genesisDate = "2020-09-21",
            algorithm = "Snowman consensus",
            categories = listOf("Layer 1", "Smart contract platform", "Interoperability"),
            tagline = "a platform of customisable subnets with fast finality",
            extra = "Avalanche splits execution, settlement and validation across dedicated chains. " +
                "Subnets let teams launch application-specific networks with their own rules and fees.",
            homepage = "https://www.avax.network",
            githubUrl = "https://github.com/ava-labs",
            subredditUrl = "https://www.reddit.com/r/Avax/",
        ),
        CoinSeed(
            id = "chainlink",
            symbol = "LINK",
            name = "Chainlink",
            imageUrl = "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png",
            price = 24.36,
            change24h = 1.47,
            change7d = 3.38,
            marketCap = 15_300_000_000,
            volume24h = 830_000_000,
            circulatingSupply = 626_000_000.0,
            maxSupply = 1_000_000_000.0,
            ath = 52.70,
            atl = 0.148,
            genesisDate = "2017-09-20",
            algorithm = "Proof of Stake",
            categories = listOf("Oracle", "Infrastructure", "DeFi"),
            tagline = "a decentralised oracle network connecting blockchains to real-world data",
            extra = "Chainlink data feeds aggregate independent node operators and are used by DeFi " +
                "protocols for pricing, lending and derivatives. Cross-chain messaging extends the " +
                "network beyond a single ledger.",
            homepage = "https://chain.link",
            githubUrl = "https://github.com/smartcontractkit",
            subredditUrl = "https://www.reddit.com/r/Chainlink/",
        ),
        CoinSeed(
            id = "shiba-inu",
            symbol = "SHIB",
            name = "Shiba Inu",
            imageUrl = "https://assets.coingecko.com/coins/images/11939/large/shiba.png",
            price = 0.00002418,
            change24h = -3.18,
            change7d = 7.21,
            marketCap = 14_300_000_000,
            volume24h = 1_100_000_000,
            circulatingSupply = 589_000_000_000_000.0,
            maxSupply = null,
            ath = 0.00008845,
            atl = 0.00000000008,
            genesisDate = "2020-08-01",
            algorithm = "ERC-20",
            categories = listOf("Meme", "Token"),
            tagline = "a meme token that grew into a broad community ecosystem",
            extra = "Shiba Inu began as an ERC-20 token and later added ShibaSwap, a burn mechanism and " +
                "its own layer-2 network. Community activity remains the main driver of its adoption.",
            homepage = "https://shibatoken.com",
            githubUrl = null,
            subredditUrl = "https://www.reddit.com/r/SHIBArmy/",
        ),
        CoinSeed(
            id = "stellar",
            symbol = "XLM",
            name = "Stellar",
            imageUrl = "https://assets.coingecko.com/coins/images/100/large/Stellar_symbol_black_RGB.png",
            price = 0.4281,
            change24h = 0.62,
            change7d = -1.27,
            marketCap = 12_900_000_000,
            volume24h = 310_000_000,
            circulatingSupply = 30_100_000_000.0,
            maxSupply = 50_000_000_000.0,
            ath = 0.8754,
            atl = 0.00122,
            genesisDate = "2014-08-01",
            algorithm = "Stellar Consensus Protocol",
            categories = listOf("Payment", "Layer 1", "Cross-border"),
            tagline = "a payment network built for low-cost cross-border transfers",
            extra = "Stellar anchors issue tokenised fiat and connect remittance corridors. Its built-in " +
                "decentralised exchange lets users convert between issued assets directly on-chain.",
            homepage = "https://stellar.org",
            githubUrl = "https://github.com/stellar",
            subredditUrl = "https://www.reddit.com/r/Stellar/",
        ),
        CoinSeed(
            id = "sui",
            symbol = "SUI",
            name = "Sui",
            imageUrl = "https://assets.coingecko.com/coins/images/26375/large/sui_asset.jpeg",
            price = 4.36,
            change24h = 5.41,
            change7d = 14.82,
            marketCap = 12_900_000_000,
            volume24h = 1_400_000_000,
            circulatingSupply = 2_950_000_000.0,
            maxSupply = 10_000_000_000.0,
            ath = 5.35,
            atl = 0.364,
            genesisDate = "2023-05-03",
            algorithm = "Delegated Proof of Stake",
            categories = listOf("Layer 1", "Smart contract platform", "Gaming"),
            tagline = "an object-centric blockchain built with the Move language",
            extra = "Sui processes simple transfers in parallel and uses owned objects to avoid global " +
                "contention. The design targets games, social apps and high-frequency consumer use cases.",
            homepage = "https://sui.io",
            githubUrl = "https://github.com/MystenLabs",
            subredditUrl = "https://www.reddit.com/r/Sui/",
        ),
        CoinSeed(
            id = "toncoin",
            symbol = "TON",
            name = "Toncoin",
            imageUrl = "https://assets.coingecko.com/coins/images/17980/large/ton_symbol.png",
            price = 5.12,
            change24h = -0.73,
            change7d = 2.11,
            marketCap = 12_700_000_000,
            volume24h = 320_000_000,
            circulatingSupply = 2_470_000_000.0,
            maxSupply = null,
            ath = 8.25,
            atl = 0.52,
            genesisDate = "2021-05-01",
            algorithm = "Proof of Stake",
            categories = listOf("Layer 1", "Payment", "Social"),
            tagline = "the native asset of The Open Network, a chain designed for mass adoption",
            extra = "TON uses sharding to scale horizontally and integrates with Telegram-based wallets " +
                "and mini apps. Its ecosystem focuses on payments, gaming and social products.",
            homepage = "https://ton.org",
            githubUrl = "https://github.com/ton-blockchain",
            subredditUrl = "https://www.reddit.com/r/toncoin/",
        ),
        CoinSeed(
            id = "polkadot",
            symbol = "DOT",
            name = "Polkadot",
            imageUrl = "https://assets.coingecko.com/coins/images/12171/large/polkadot.png",
            price = 7.84,
            change24h = -1.09,
            change7d = 0.86,
            marketCap = 12_000_000_000,
            volume24h = 410_000_000,
            circulatingSupply = 1_530_000_000.0,
            maxSupply = null,
            ath = 54.98,
            atl = 2.70,
            genesisDate = "2020-05-26",
            algorithm = "Nominated Proof of Stake",
            categories = listOf("Layer 0", "Interoperability", "Proof of Stake"),
            tagline = "a relay chain that connects specialised parachains",
            extra = "Polkadot shares security across connected chains through a central relay chain. " +
                "Parachains can specialise for DeFi, identity or gaming while communicating natively.",
            homepage = "https://polkadot.network",
            githubUrl = "https://github.com/paritytech/polkadot-sdk",
            subredditUrl = "https://www.reddit.com/r/Polkadot/",
        ),
        CoinSeed(
            id = "bitcoin-cash",
            symbol = "BCH",
            name = "Bitcoin Cash",
            imageUrl = "https://assets.coingecko.com/coins/images/780/large/bitcoin-cash-circle.png",
            price = 542.10,
            change24h = 2.84,
            change7d = 5.19,
            marketCap = 10_700_000_000,
            volume24h = 520_000_000,
            circulatingSupply = 19_800_000.0,
            maxSupply = 21_000_000.0,
            ath = 3_785.82,
            atl = 76.93,
            genesisDate = "2017-08-01",
            algorithm = "SHA-256",
            categories = listOf("Layer 1", "Payment", "Proof of Work"),
            tagline = "a Bitcoin fork focused on larger blocks and cheap payments",
            extra = "Bitcoin Cash split from Bitcoin in 2017 to raise the block-size limit and keep " +
                "on-chain fees low. It positions itself as everyday spending money rather than a " +
                "settlement layer only.",
            homepage = "https://bitcoincash.org",
            githubUrl = "https://github.com/bitcoin-cash-node",
            subredditUrl = "https://www.reddit.com/r/bitcoincash/",
        ),
        CoinSeed(
            id = "litecoin",
            symbol = "LTC",
            name = "Litecoin",
            imageUrl = "https://assets.coingecko.com/coins/images/2/large/litecoin.png",
            price = 118.40,
            change24h = 0.37,
            change7d = -0.94,
            marketCap = 8_900_000_000,
            volume24h = 640_000_000,
            circulatingSupply = 75_200_000.0,
            maxSupply = 84_000_000.0,
            ath = 410.26,
            atl = 1.15,
            genesisDate = "2011-10-07",
            algorithm = "Scrypt",
            categories = listOf("Layer 1", "Payment", "Proof of Work"),
            tagline = "one of the earliest Bitcoin alternatives, tuned for faster blocks",
            extra = "Litecoin uses Scrypt mining and a four-times higher supply cap than Bitcoin. " +
                "It served as a testbed for upgrades such as SegWit and the Lightning Network.",
            homepage = "https://litecoin.org",
            githubUrl = "https://github.com/litecoin-project/litecoin",
            subredditUrl = "https://www.reddit.com/r/litecoin/",
        ),
        CoinSeed(
            id = "uniswap",
            symbol = "UNI",
            name = "Uniswap",
            imageUrl = "https://assets.coingecko.com/coins/images/12504/large/uniswap-logo.png",
            price = 14.87,
            change24h = -2.76,
            change7d = 1.92,
            marketCap = 8_900_000_000,
            volume24h = 480_000_000,
            circulatingSupply = 600_000_000.0,
            maxSupply = 1_000_000_000.0,
            ath = 44.97,
            atl = 1.03,
            genesisDate = "2020-09-16",
            algorithm = "ERC-20",
            categories = listOf("DeFi", "DEX", "Governance"),
            tagline = "the governance token of the largest automated market maker",
            extra = "Uniswap lets anyone swap tokens against liquidity pools instead of an order book. " +
                "UNI holders can vote on fee changes, treasury spending and protocol upgrades.",
            homepage = "https://uniswap.org",
            githubUrl = "https://github.com/Uniswap",
            subredditUrl = "https://www.reddit.com/r/Uniswap/",
        ),
        CoinSeed(
            id = "near",
            symbol = "NEAR",
            name = "NEAR Protocol",
            imageUrl = "https://assets.coingecko.com/coins/images/10365/large/near.jpg",
            price = 5.67,
            change24h = 3.12,
            change7d = 8.44,
            marketCap = 6_900_000_000,
            volume24h = 340_000_000,
            circulatingSupply = 1_210_000_000.0,
            maxSupply = null,
            ath = 20.42,
            atl = 0.53,
            genesisDate = "2020-04-22",
            algorithm = "Nightshade",
            categories = listOf("Layer 1", "Smart contract platform", "AI"),
            tagline = "a sharded, developer-friendly smart contract platform",
            extra = "NEAR uses sharding and human-readable account names to simplify onboarding. " +
                "It has become a hub for AI agents and chain-abstraction experiments.",
            homepage = "https://near.org",
            githubUrl = "https://github.com/near",
            subredditUrl = "https://www.reddit.com/r/nearprotocol/",
        ),
        CoinSeed(
            id = "polygon-ecosystem-token",
            symbol = "POL",
            name = "Polygon",
            imageUrl = "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png",
            price = 0.6842,
            change24h = -1.42,
            change7d = 3.66,
            marketCap = 6_800_000_000,
            volume24h = 440_000_000,
            circulatingSupply = 10_000_000_000.0,
            maxSupply = 10_000_000_000.0,
            ath = 2.92,
            atl = 0.32,
            genesisDate = "2019-04-28",
            algorithm = "Proof of Stake",
            categories = listOf("Layer 2", "Scaling", "Interoperability"),
            tagline = "a scaling ecosystem that grew from the Matic sidechain",
            extra = "Polygon offers PoS, zkEVM and AggLayer components to scale Ethereum. POL is the " +
                "native token used for staking, fees and governance across that ecosystem.",
            homepage = "https://polygon.technology",
            githubUrl = "https://github.com/maticnetwork",
            subredditUrl = "https://www.reddit.com/r/0xPolygon/",
        ),
        CoinSeed(
            id = "monero",
            symbol = "XMR",
            name = "Monero",
            imageUrl = "https://assets.coingecko.com/coins/images/69/large/monero_logo.png",
            price = 342.75,
            change24h = 0.88,
            change7d = -2.31,
            marketCap = 6_300_000_000,
            volume24h = 180_000_000,
            circulatingSupply = 18_400_000.0,
            maxSupply = null,
            ath = 542.33,
            atl = 0.21,
            genesisDate = "2014-04-18",
            algorithm = "RandomX",
            categories = listOf("Privacy", "Layer 1", "Proof of Work"),
            tagline = "a privacy-focused cryptocurrency with default confidential transactions",
            extra = "Monero hides sender, receiver and amount using ring signatures, stealth addresses " +
                "and confidential transactions. Its tail emission keeps a permanent incentive for miners.",
            homepage = "https://www.getmonero.org",
            githubUrl = "https://github.com/monero-project/monero",
            subredditUrl = "https://www.reddit.com/r/Monero/",
        ),
        CoinSeed(
            id = "aptos",
            symbol = "APT",
            name = "Aptos",
            imageUrl = "https://assets.coingecko.com/coins/images/26455/large/aptos_round.png",
            price = 9.84,
            change24h = 2.03,
            change7d = 6.73,
            marketCap = 6_100_000_000,
            volume24h = 390_000_000,
            circulatingSupply = 620_000_000.0,
            maxSupply = null,
            ath = 19.90,
            atl = 3.08,
            genesisDate = "2022-10-17",
            algorithm = "AptosBFT",
            categories = listOf("Layer 1", "Smart contract platform", "Move"),
            tagline = "a parallel-execution blockchain built by former Diem engineers",
            extra = "Aptos uses the Move language and Block-STM to execute independent transactions in " +
                "parallel. The network targets payments, gaming and institutional use cases.",
            homepage = "https://aptoslabs.com",
            githubUrl = "https://github.com/aptos-labs",
            subredditUrl = "https://www.reddit.com/r/Aptos/",
        ),
        CoinSeed(
            id = "internet-computer",
            symbol = "ICP",
            name = "Internet Computer",
            imageUrl = "https://assets.coingecko.com/coins/images/14495/large/dfinity.png",
            price = 12.45,
            change24h = -4.11,
            change7d = -3.48,
            marketCap = 5_900_000_000,
            volume24h = 210_000_000,
            circulatingSupply = 474_000_000.0,
            maxSupply = null,
            ath = 700.65,
            atl = 2.87,
            genesisDate = "2021-05-10",
            algorithm = "Chain-key cryptography",
            categories = listOf("Layer 1", "Cloud", "Infrastructure"),
            tagline = "a blockchain that runs web services directly on-chain",
            extra = "The Internet Computer hosts smart contracts called canisters that can serve web " +
                "content without traditional cloud infrastructure. Chain-key cryptography keeps " +
                "subnets interoperable.",
            homepage = "https://internetcomputer.org",
            githubUrl = "https://github.com/dfinity",
            subredditUrl = "https://www.reddit.com/r/dfinity/",
        ),
        CoinSeed(
            id = "pepe",
            symbol = "PEPE",
            name = "Pepe",
            imageUrl = "https://assets.coingecko.com/coins/images/29850/large/pepe-token.jpeg",
            price = 0.00001284,
            change24h = 6.27,
            change7d = 18.35,
            marketCap = 5_400_000_000,
            volume24h = 1_800_000_000,
            circulatingSupply = 420_690_000_000_000.0,
            maxSupply = null,
            ath = 0.00002825,
            atl = 0.00000005,
            genesisDate = "2023-04-17",
            algorithm = "ERC-20",
            categories = listOf("Meme", "Token"),
            tagline = "a meme coin inspired by the Pepe the Frog internet character",
            extra = "Pepe launched with a large share of supply in a liquidity pool and no presale. " +
                "It is one of the most traded meme tokens and relies entirely on community momentum.",
            homepage = "https://www.pepe.vip",
            githubUrl = null,
            subredditUrl = "https://www.reddit.com/r/pepecoin/",
        ),
        CoinSeed(
            id = "arbitrum",
            symbol = "ARB",
            name = "Arbitrum",
            imageUrl = "https://assets.coingecko.com/coins/images/16547/large/arb.jpg",
            price = 1.02,
            change24h = -1.88,
            change7d = 2.54,
            marketCap = 4_300_000_000,
            volume24h = 380_000_000,
            circulatingSupply = 4_200_000_000.0,
            maxSupply = 10_000_000_000.0,
            ath = 2.40,
            atl = 0.29,
            genesisDate = "2023-03-23",
            algorithm = "Optimistic rollup",
            categories = listOf("Layer 2", "Scaling", "Governance"),
            tagline = "an optimistic rollup ecosystem scaling Ethereum",
            extra = "Arbitrum batches transactions off-chain and posts compressed proofs to Ethereum. " +
                "ARB governs the DAO that funds the ecosystem and steers protocol upgrades.",
            homepage = "https://arbitrum.io",
            githubUrl = "https://github.com/OffchainLabs",
            subredditUrl = "https://www.reddit.com/r/arbitrum/",
        ),
        CoinSeed(
            id = "optimism",
            symbol = "OP",
            name = "Optimism",
            imageUrl = "https://assets.coingecko.com/coins/images/25244/large/op.jpeg",
            price = 2.14,
            change24h = 0.54,
            change7d = -1.63,
            marketCap = 3_600_000_000,
            volume24h = 240_000_000,
            circulatingSupply = 1_680_000_000.0,
            maxSupply = 4_294_967_296.0,
            ath = 4.85,
            atl = 0.40,
            genesisDate = "2022-05-31",
            algorithm = "Optimistic rollup",
            categories = listOf("Layer 2", "Scaling", "Governance"),
            tagline = "a rollup built around the OP Stack and shared public-goods funding",
            extra = "Optimism's OP Stack powers a growing superchain of compatible L2 networks. " +
                "A portion of sequencer revenue is routed to retroactive public-goods funding.",
            homepage = "https://optimism.io",
            githubUrl = "https://github.com/ethereum-optimism",
            subredditUrl = "https://www.reddit.com/r/optimismCollective/",
        ),
        CoinSeed(
            id = "filecoin",
            symbol = "FIL",
            name = "Filecoin",
            imageUrl = "https://assets.coingecko.com/coins/images/12817/large/filecoin.png",
            price = 4.96,
            change24h = -2.94,
            change7d = -5.17,
            marketCap = 3_100_000_000,
            volume24h = 190_000_000,
            circulatingSupply = 625_000_000.0,
            maxSupply = 1_960_000_000.0,
            ath = 236.84,
            atl = 2.64,
            genesisDate = "2020-10-15",
            algorithm = "Proof of Replication",
            categories = listOf("Storage", "Infrastructure", "DeFi"),
            tagline = "a decentralised storage network with cryptographic proofs",
            extra = "Filecoin miners prove they keep client data over time and earn block rewards for it. " +
                "The network is used for archival storage, dApp backends and data DAOs.",
            homepage = "https://filecoin.io",
            githubUrl = "https://github.com/filecoin-project",
            subredditUrl = "https://www.reddit.com/r/filecoin/",
        ),
        CoinSeed(
            id = "cosmos",
            symbol = "ATOM",
            name = "Cosmos Hub",
            imageUrl = "https://assets.coingecko.com/coins/images/1481/large/cosmos_hub.png",
            price = 7.32,
            change24h = 1.26,
            change7d = 0.42,
            marketCap = 2_800_000_000,
            volume24h = 160_000_000,
            circulatingSupply = 390_000_000.0,
            maxSupply = null,
            ath = 44.70,
            atl = 1.13,
            genesisDate = "2019-03-13",
            algorithm = "Tendermint BFT",
            categories = listOf("Interoperability", "Layer 0", "Proof of Stake"),
            tagline = "the first hub in the interoperable Cosmos network",
            extra = "Cosmos popularised the Tendermint consensus and the IBC messaging protocol. " +
                "Sovereign app-chains can exchange assets and data with the Hub and with each other.",
            homepage = "https://cosmos.network",
            githubUrl = "https://github.com/cosmos",
            subredditUrl = "https://www.reddit.com/r/cosmosnetwork/",
        ),
    )

    fun buildCoins(): List<Coin> = seeds.mapIndexed { index, seed -> seed.toCoin(rank = index + 1) }

    fun detailsFor(coin: Coin): CoinDetails {
        val seed = seeds.first { it.id == coin.id }
        val supplyText = if (coin.maxSupply != null) {
            "The supply is capped at ${compact(coin.maxSupply)} ${seed.symbol}, and " +
                "${compact(coin.circulatingSupply)} ${seed.symbol} is already circulating."
        } else {
            "${compact(coin.circulatingSupply)} ${seed.symbol} is currently circulating and there is no hard supply cap."
        }
        val description = buildString {
            append("${seed.name} (${seed.symbol}) is ${seed.tagline}. ")
            append("The project launched on ${seed.genesisDate} and ranks #${coin.rank} by market capitalisation.\n\n")
            append(seed.extra)
            append("\n\n")
            append("The network relies on ${seed.algorithm ?: "its native consensus mechanism"}. ")
            append(supplyText)
        }
        return CoinDetails(
            coin = coin,
            description = description,
            homepage = seed.homepage,
            githubUrl = seed.githubUrl,
            subredditUrl = seed.subredditUrl,
            categories = seed.categories,
            genesisDate = seed.genesisDate,
            hashingAlgorithm = seed.algorithm,
            change30d = longTermChange(seed, 30),
            change60d = longTermChange(seed, 60),
            change1y = longTermChange(seed, 365),
            fullyDilutedValuation = coin.maxSupply
                ?.let { (it * coin.price).toLong() }
                ?: (coin.marketCap * 1.18).toLong(),
            totalSupply = coin.maxSupply ?: coin.circulatingSupply,
            lastUpdated = "Updated just now",
        )
    }

    /**
     * Builds a deterministic price series for the detail chart. The chart is prefetched
     * from `/coins/{id}/market_chart?vs_currency=usd&days=...` in a real implementation.
     */
    fun chartPoints(coin: Coin, range: ChartRange): List<Double> {
        val (change, points) = when (range) {
            ChartRange.DAY -> coin.change24h to 40
            ChartRange.WEEK -> coin.change7d to 56
            ChartRange.MONTH -> longTermChange(coin, 30) to 72
            ChartRange.YEAR -> longTermChange(coin, 365) to 96
        }
        val random = Random(coin.id.hashCode().toLong() * 17L + range.ordinal * 101L)
        val start = (coin.price / (1 + change / 100.0)).coerceAtLeast(coin.price * 0.05)
        return (0 until points).map { i ->
            val progress = i.toDouble() / (points - 1)
            val trend = start + (coin.price - start) * progress
            val waveAmplitude = when (range) {
                ChartRange.DAY -> 0.004
                ChartRange.WEEK -> 0.010
                ChartRange.MONTH -> 0.020
                ChartRange.YEAR -> 0.038
            }
            val wave = sin(progress * PI * random.nextInt(2, 5)) * waveAmplitude
            val noise = random.nextDouble(-waveAmplitude, waveAmplitude)
            (trend * (1 + wave + noise)).coerceAtLeast(coin.price * 0.02)
        }.toMutableList().apply { this[lastIndex] = coin.price }
    }

    private fun longTermChange(seed: CoinSeed, days: Int): Double {
        val multiplier = when (days) {
            30 -> 2.4
            60 -> 3.4
            else -> 6.1
        }
        val jitter = (seed.id.hashCode().absoluteValue % 29) - 14.0
        return (seed.change7d * multiplier + jitter).coerceIn(-72.0, 185.0)
    }

    private fun longTermChange(coin: Coin, days: Int): Double {
        val seed = seeds.first { it.id == coin.id }
        return longTermChange(seed, days)
    }

    private fun CoinSeed.toCoin(rank: Int): Coin {
        val sparkline = buildSparkline(id, price, change7d)
        val recent = sparkline.takeLast(6)
        return Coin(
            id = id,
            rank = rank,
            symbol = symbol,
            name = name,
            imageUrl = imageUrl,
            price = price,
            change24h = change24h,
            change7d = change7d,
            marketCap = marketCap,
            volume24h = volume24h,
            high24h = max(price * 1.006, recent.maxOrNull() ?: price),
            low24h = min(price * 0.994, recent.minOrNull() ?: price),
            circulatingSupply = circulatingSupply,
            maxSupply = maxSupply,
            ath = ath,
            atl = atl,
            sparkline = sparkline,
        )
    }

    private fun buildSparkline(id: String, endPrice: Double, change7d: Double): List<Double> {
        val random = Random(id.hashCode().toLong() * 31L + 7L)
        val points = 32
        val start = endPrice / (1 + change7d / 100.0)
        return (0 until points).map { i ->
            val progress = i.toDouble() / (points - 1)
            val trend = start + (endPrice - start) * progress
            val wave = sin(progress * PI * random.nextDouble(1.5, 4.0)) * 0.006
            val noise = random.nextDouble(-0.008, 0.008)
            (trend * (1 + wave + noise)).coerceAtLeast(endPrice * 0.05)
        }.toMutableList().apply { this[lastIndex] = endPrice }
    }

    private fun compact(value: Double): String = when {
        value >= 1_000_000_000_000 -> String.format(Locale.US, "%.2fT", value / 1_000_000_000_000)
        value >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", value / 1_000_000_000)
        value >= 1_000_000 -> String.format(Locale.US, "%.2fM", value / 1_000_000)
        value >= 1_000 -> String.format(Locale.US, "%.2fK", value / 1_000)
        else -> String.format(Locale.US, "%.2f", value)
    }
}
